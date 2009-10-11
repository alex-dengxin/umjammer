/*
 * Copyright (c) 2001 Brian Boyter
 * All rights reserved
 *
 * This software is released subject to the GNU Public License.  See
 * the full license included with this distribution.
 */

#include <windows.h>
#include <winerror.h>
#include <stdio.h>
#include <wincrypt.h>
#include <jni.h>
#include <wininet.h>
#include <sys/stat.h>
#include <io.h>
#include <fcntl.h>
#include "com_boyter_mscrypto_MSCryptoManager.h"

#include "cygsupport.h"


static HCERTSTORE hSystemStore = 0;
static PCCERT_CONTEXT pCertContext = NULL;

static byte* alias = NULL;
static char* CSP = MS_ENHANCED_PROV;

// define structures used for a linked-list
typedef struct LISTENTRY {
    char *next;
    int nbytes;
    BYTE *data;
} ListEntry;

typedef struct LIST {
    ListEntry *first;
    ListEntry *last;
} List;

//
static void throwCryptoException(JNIEnv *env, char *gripe, DWORD errnum);
static void throwException(JNIEnv *env, char* className, char* message);
static void base64();
static char* strconvert();
static void reversecopy();
static void AddDataToList(List *list, BYTE *data, int datalength);


/**
 * Class:     com_boyter_mscrypto_MSCryptoManager
 * Method:    getCACerts
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_boyter_mscrypto_MSCryptoManager_getCACerts
 (JNIEnv *env, jobject this_obj)
{
    int ncerts = 0;
    int nout;
    List list;
    BYTE *certblob;
    ListEntry *lp;
    jclass sclass;
    jobjectArray jcerts = NULL;
    jobject jobj;
    jobject jcert;
    char *encodedcert;
    int nbytesout;
   
//printf("Function MSgetCACerts entered\n");
    list.first = NULL;
    list.last  = NULL;

    // open the system Root CA store
    if (!(hSystemStore = CertOpenStore((LPCSTR) CERT_STORE_PROV_SYSTEM, 0, 0, CERT_SYSTEM_STORE_CURRENT_USER, L"Root"))) {
        throwCryptoException(env, "Could not open cert store", 0);
        return NULL;
    }

    ncerts = 0;    
    // read the next cert
    while ((pCertContext = CertEnumCertificatesInStore(hSystemStore, pCertContext)) != 0) {
        certblob = malloc(pCertContext->cbCertEncoded);
        memcpy(certblob, pCertContext->pbCertEncoded, pCertContext->cbCertEncoded);

        // add to list of certs
        ncerts++;
        AddDataToList(&list, certblob, pCertContext->cbCertEncoded);
    }
    CertCloseStore(hSystemStore, 0);

    // open the system Sub CA store
    if (!(hSystemStore = CertOpenStore((LPCSTR) CERT_STORE_PROV_SYSTEM, 0, 0, CERT_SYSTEM_STORE_CURRENT_USER, L"CA"))) {
        throwCryptoException(env, "Could not open cert store", 0);
        return NULL;
    }

    // read the next cert
    while ((pCertContext = CertEnumCertificatesInStore(hSystemStore, pCertContext)) != 0) {
        certblob = malloc(pCertContext->cbCertEncoded);
        memcpy(certblob, pCertContext->pbCertEncoded, pCertContext->cbCertEncoded);

        // add to list of certs
        ncerts++;
        AddDataToList(&list, certblob, pCertContext->cbCertEncoded);
    }
    CertCloseStore(hSystemStore, 0);
    CertFreeCertificateContext(pCertContext);

//printf("MSgetCACerts:  %d CA certs found\n", ncerts);

    jobj = (*env)->NewStringUTF(env, "");
    if (jobj == NULL) {
    	throwException(env, "java/lang/OutOfMemoryError", "jobj");
    }
    sclass = (*env)->FindClass(env, "java/lang/String");
    if (sclass == NULL) {
    	throwException(env, "java/lang/OutOfMemoryError", "sclass");
    }
    jcerts = (*env)->NewObjectArray (env, ncerts, sclass, jobj);
    if (jcerts == NULL) {
    	throwException(env, "java/lang/OutOfMemoryError", "jcerts");
    }

    lp = list.first;

    /*
     * the next part works, but what I really wanted to do was pass
     * an array of certificates, each cert being a byte[], back to the
     * java program...   I couldn't get the byte[][] stuff to work, so
     * instead I base64-encode the certs which converts them to a
     * String[].
     */
    for (nout = 0; nout < ncerts; nout++) {
//printf("MSgetCACerts:  cert %d found len %d\n", nout, lp->nbytes);

        nbytesout = (((lp->nbytes) + 2) / 3) * 4;    // size of base64-encoded cert
        encodedcert = malloc(nbytesout + 56);
        strcpy(encodedcert, "-----BEGIN CERTIFICATE-----\n");
        base64(lp->data, (int) (lp->nbytes), encodedcert + 28, nbytesout, nout);
        strcat(encodedcert, "\n-----END CERTIFICATE-----\n");

//printf("\nencoded cert len: %d\n%s\n", strlen(encodedcert), encodedcert);

        jcert = (*env)->NewStringUTF(env, encodedcert);
        if (jcert == NULL) {
            throwCryptoException(env, "bad thing happened - jcert null - size: %d", nbytesout);
        }

        (*env)->SetObjectArrayElement(env, jcerts, nout, jcert);
        free(lp->data);
        lp = (ListEntry *)(lp->next);
        if (lp == NULL) {
            break;
        }
    }

//printf("return from MSgetCACerts:  %d certs found\n", ncerts);
    return jcerts;
}


/**
 * MSVerifyCertRevocation.
 * @return 1: the cert is good
 *         0: the cert is bad
 *        -1: the query timed out
 *        -2: processing error
 */
JNIEXPORT jint JNICALL Java_com_boyter_mscrypto_MSCryptoManager_verifyCertRevocation
 (JNIEnv *env, jobject this_object, jbyteArray jCert)
{
    DWORD CertNameLen;
    BYTE  CertName[512];
    PCCERT_CONTEXT pCertContext;
    jsize CertLen;
    BYTE *Cert;
    DWORD CertNameFormat;
    PVOID rgpvContext[1];
    CERT_REVOCATION_STATUS status;
    char *reason = NULL;

//printf("MSVerifyCertRevocation: entered\n");

    CertLen = (*env)->GetArrayLength(env, jCert);
    Cert =    (*env)->GetByteArrayElements(env, jCert, NULL);

    pCertContext = CertCreateCertificateContext(X509_ASN_ENCODING, Cert, CertLen);
    (*env)->ReleaseByteArrayElements(env, jCert, Cert, 0);
    if (pCertContext == NULL) {
        throwCryptoException(env, "unable to create cert context", 0L);
        return -1;
    }

    CertNameLen = sizeof(CertName);
    CertNameFormat = CERT_SIMPLE_NAME_STR;
    CertGetNameString(pCertContext, CERT_NAME_RDN_TYPE, 0, &CertNameFormat, CertName, CertNameLen);
//printf("\nchecking revocation status for cert: %s", CertName);

    rgpvContext[0] = (PVOID)pCertContext;
    status.cbSize = sizeof(CERT_REVOCATION_STATUS);

//printf("MSVerifyCertRevocation: call function CertVerifyRevocation\n");

    if (CertVerifyRevocation(X509_ASN_ENCODING, CERT_CONTEXT_REVOCATION_TYPE, 1, rgpvContext, 0, NULL, &status)) {
//printf("\nMSVerifyCertRevocation: cert is not revoked\n");
            return 1; // cert is not revoked
    }

    if (status.dwError == CRYPT_E_REVOKED) {
        switch (status.dwReason) {
        case 0:
            reason = "unspecified";
            break;
        case 1:
            reason = "KEY_COMPROMISE";
            break;
        case 2:
            reason = "CA_COMPROMISE";
            break;
        case 3:
            reason = "AFFILIATION_CHANGED";
            break;
        case 4:
            reason = "SUPERSEDED";
            break;
        case 5:
            reason = "CESSATION_OF_OPERATION";
            break;
        case 6:
            reason = "CERTIFICATE_HOLD";
        }

fprintf(stderr, "MSVerifyCertRevocation: - CERT REVOKED - %s\n", reason);    // cert is revoked
fflush(stderr);
        return 0;        // cert is revoked
    }

    switch (status.dwError) {
    case CRYPT_E_NO_REVOCATION_CHECK:
        reason = "NO_REVOCATION_CHECK";
        break;
    case CRYPT_E_NO_REVOCATION_DLL:
        reason = "NO_REVOCATION_DLL";
        break;
    case CRYPT_E_NOT_IN_REVOCATION_DATABASE:
        reason = "NOT_IN_REVOCATION_DATABASE";
        break;
    case CRYPT_E_REVOCATION_OFFLINE:
        reason = "REVOCATION_OFFLINE";
        break;
    case E_INVALIDARG:
        reason = "INVALIDARG";
        break;
    case ERROR_SUCCESS:
        reason = "ERROR_SUCCESS";
    }

fprintf(stderr, "MSVerifyCertRevocation: - PROCESSING ERROR - %s\n", reason);
fflush(stderr);
    return -2;
}

/**
 * returns priv key for given alias
 */
JNIEXPORT jbyteArray JNICALL Java_com_boyter_mscrypto_MSCryptoManager_getPrivateKey
    (JNIEnv *env, jobject this_object, jstring jalias) {

    jbyteArray pkey;
    BYTE *keybuf;
    DWORD keybuflen;
    HCRYPTKEY UserKey;
    HCRYPTPROV hProv;
    const jbyte *xalias;
//    BOOL dummykeyflag = FALSE;
    BYTE *GenerateDummyPrivKey();

    xalias = (*env)->GetStringUTFChars(env, jalias, FALSE);
    alias = strdup(xalias);

//printf("Entered function MSgetPrivateKey, alias: %s\n", alias);

    if (!CryptAcquireContext(&hProv, alias, CSP, PROV_RSA_FULL, 0)) {
        throwCryptoException(env, "CryptAcquireContext", GetLastError());
        return NULL;
    }

    if (!CryptGetUserKey(hProv, AT_KEYEXCHANGE, &UserKey)) {
        throwCryptoException(env, "getting signature key", GetLastError());
        return NULL;
    }

    if (!CryptExportKey(UserKey, 0, PRIVATEKEYBLOB, 0, NULL, &keybuflen)) {
        if (GetLastError() == NTE_BAD_KEY_STATE) {
            throwCryptoException(env, "The private key cannot be exported - return dummy key", NTE_BAD_KEY_STATE);
            CryptReleaseContext(hProv, 0);
            hProv = 0;
            return NULL;
        } else {
            throwCryptoException(env, "getting signature key length", GetLastError());
            return NULL;
        }
    } else {
//printf("priv key blob size: %d\n", keybuflen);

        keybuf = malloc(keybuflen);
        if (!CryptExportKey(UserKey, 0, PRIVATEKEYBLOB, 0, keybuf, &keybuflen)) {
            throwCryptoException(env, "getting signature key", GetLastError());
            return NULL;
        }
    }

    pkey = (*env)->NewByteArray(env, keybuflen);
    (*env)->SetByteArrayRegion(env, pkey, 0, keybuflen, keybuf);
    free(keybuf);

    if (hProv) {
        CryptReleaseContext(hProv, 0);
        hProv = 0;
    }

    return pkey;
}

/**
 *
 */
JNIEXPORT jbyteArray JNICALL Java_com_boyter_mscrypto_MSCryptoManager_getCert
 (JNIEnv *env, jobject this_object, jstring jcertStore, jstring jalias) {

    const jbyte* xalias;
    const jchar* certStore;
    HCERTSTORE hSystemStore;
    PCCERT_CONTEXT pCertContext = NULL;
    BYTE* pbData;
    DWORD cbData;
    BOOL match = FALSE;
    HCRYPTPROV hProv;
    DWORD KeySpec;
    BOOL CallerFreeProv;
    DWORD CertNameLen;
    BYTE CertName[1024];
    jbyteArray jcertblob;

    xalias    = (*env)->GetStringUTFChars(env, jalias, FALSE);
    certStore = (*env)->GetStringChars(env, jcertStore, FALSE);
    alias = strdup(xalias);

//printf("Entered function MSgetCert: %S %s\n", certStore, alias);

    // open the certstore
    if (!(hSystemStore = CertOpenStore((LPCSTR) CERT_STORE_PROV_SYSTEM, 0, 0, CERT_SYSTEM_STORE_CURRENT_USER, certStore))) {
        throwCryptoException(env, "Could not open cert store", 0L);
        return FALSE;
    }

    // read every cert in the store looking for a match
    while ((pCertContext = CertEnumCertificatesInStore(hSystemStore, pCertContext)) != 0) {

        CertNameLen = sizeof(CertName);
        if (CertGetNameString(pCertContext, CERT_NAME_SIMPLE_DISPLAY_TYPE, 0, NULL, CertName, CertNameLen) < 2) {
            throwCryptoException(env, "CertNameToStr", GetLastError());
            return NULL;
        }
//printf("getCert: cert name: %s\n", CertName);

        if (!CryptAcquireCertificatePrivateKey(pCertContext, CRYPT_ACQUIRE_CACHE_FLAG, NULL, &hProv, &KeySpec, &CallerFreeProv)) {
            throwCryptoException(env, "CryptAcquireCertificatePrivateKey", GetLastError());
            continue;
        }

        // Read the name of the default key container.
        cbData = 0;
        if (!CryptGetProvParam(hProv, PP_CONTAINER, NULL, &cbData, 0)) {
            throwCryptoException(env, "reading key container name", GetLastError());
            continue;
        }

        pbData = malloc(cbData);
        if(!CryptGetProvParam(hProv, PP_CONTAINER, pbData, &cbData, 0)) {
            throwCryptoException(env, "reading key container name", GetLastError());
            continue;
        }
//printf("getCert: alias name: %s\n", pbData);
        if ((strcmp(pbData, alias) == 0)) {
            match = TRUE;
            break;
        }
    }

    // no match
    CertCloseStore(hSystemStore, 0);
    if (!match) {
        return NULL;
    }

    // found a match - return
    jcertblob = (*env)->NewByteArray(env, pCertContext->cbCertEncoded);
    (*env)->SetByteArrayRegion(env, (jarray)jcertblob, 0, pCertContext->cbCertEncoded, pCertContext->pbCertEncoded);

    if (jcertblob == NULL) {
        throwException(env, "java/lang/OutOfMemoryError", "jcertblob");
    }

    CryptReleaseContext(hProv, 0);
    return jcertblob;
}

/**
 *
 */
JNIEXPORT jobjectArray JNICALL Java_com_boyter_mscrypto_MSCryptoManager_getAliases
 (JNIEnv *env, jobject this_object, jstring jcertStore) {

    DWORD CertNameLen;
    BYTE CertName[512];
    int nalias = 0;
    List list;
    BYTE *alias;
    ListEntry *lp;
    jstring jalias;
    jclass sclass;
    jobjectArray jaliases;
    const jchar* certStore;
//    DWORD KeySpec;
//    BOOL CallerFreeProv;
    static void AddDataToList();
    int nout;
    jobject jobj;
//    int aliasLen;
//    DWORD dwFlags;
//    HCRYPTPROV hProv = 0;
    DWORD propLen;
    CRYPT_KEY_PROV_INFO *certProp;

    certStore = (*env)->GetStringChars(env, jcertStore, FALSE);
//printf("Entered function MSgetAliases\n");

    list.first = NULL;
    list.last  = NULL;

    if (!(hSystemStore = CertOpenStore((LPCSTR) CERT_STORE_PROV_SYSTEM, 0, 0, CERT_SYSTEM_STORE_CURRENT_USER, certStore))) {
        throwCryptoException(env, "Could not open Cert Store", 0L);
        return FALSE;
    }

    // read the next cert
    nalias = 0;
    while ((pCertContext = CertEnumCertificatesInStore(hSystemStore, pCertContext)) != 0) {

        CertNameLen = sizeof(CertName);
        if (CertGetNameString(pCertContext, CERT_NAME_SIMPLE_DISPLAY_TYPE, 0, NULL, CertName, CertNameLen) < 2) {
            throwCryptoException(env, "CertNameToStr", GetLastError());
            continue;
        }
fprintf(stderr, " cert name: %s\n", CertName);
fflush(stderr);

        if (!CertGetCertificateContextProperty(pCertContext, CERT_KEY_PROV_INFO_PROP_ID, NULL, &propLen)) {
            throwCryptoException(env, "CertGetCertificateContextProperty", GetLastError());
            continue;
        }

        certProp = malloc(propLen);
        if (!CertGetCertificateContextProperty(pCertContext, CERT_KEY_PROV_INFO_PROP_ID, certProp, &propLen)) {
            throwCryptoException(env, "CertGetCertificateContextProperty", GetLastError());
            continue;
        }

//printf("container name: %S\n", certProp->pwszContainerName);
        alias = strconvert (certProp->pwszContainerName);

//printf("add alias: %s to the list of aliases\n", alias);

        // add to list of aliases
        nalias++;
        AddDataToList(&list, alias, strlen(alias)+1);
    }

    jobj     = (*env)->NewStringUTF(env, "");
    sclass   = (*env)->FindClass(env, "java/lang/String");
    jaliases = (*env)->NewObjectArray (env, (jsize)nalias, sclass, jobj);

    lp = list.first;
    for (nout = 0; nout < nalias; nout++) {
//printf("MSgetAliases: found %s on linked list\n", lp->data);
        jalias = (*env)->NewStringUTF(env, lp->data);
        (*env)->SetObjectArrayElement(env, jaliases, nout, jalias);
        lp = (ListEntry *) (lp->next);
        if (lp == NULL) {
            break;
        }
    }

//    CertCloseStore(hSystemStore, 0);
//    CryptReleaseContext(hProv, 0);

    return jaliases;
}

/**
 *
 */
JNIEXPORT jbyteArray JNICALL Java_com_boyter_mscrypto_MSCryptoManager_getRSASignHash
(JNIEnv *env, jobject this_obj, jbyteArray jhash, jbyteArray jprivatekey, jstring jhashalg) {
    ALG_ID HashAlg;
    const jbyte *hashalg;
    byte *sig;
    byte *csig;
    DWORD nsig = 0;
    jbyteArray sigblob;
    DWORD i;
    byte *hash;
    DWORD nhash;
    HCRYPTPROV hSignProv;
    HCRYPTHASH hSignHash;

//printf("entered function:  MSrsaSignHash\n");

    nhash   = (*env)->GetArrayLength(env, jhash);
    hash    = (*env)->GetByteArrayElements(env, jhash, NULL);
    hashalg = (*env)->GetStringUTFChars(env, jhashalg, FALSE);

//printf("   alias: %s\n", alias);
//printf("   hash alg: %s\n", hashalg);
//printf("   hash len: %d bytes\n", nhash);

    if (strcmp(hashalg, "SHA1") == 0) {
        HashAlg = CALG_SHA;
    } else if (strcmp(hashalg, "MD5") == 0) {
        HashAlg = CALG_MD5;
    } else {
        // need to throw exception
        throwException(env, "java/lang/IllegalArgumentException", "invalid hash algorithm");
        return NULL;
    }

    if(!CryptAcquireContext(&hSignProv, alias, CSP, PROV_RSA_FULL, 0L)) {
        throwCryptoException(env, "CryptAcquireContext", GetLastError());
        return NULL;
    }

    if (!CryptCreateHash(hSignProv, HashAlg, 0, 0, &hSignHash)) {
        throwCryptoException(env, "CryptCreateHash", GetLastError());
        return NULL;
    }

    if (!CryptSetHashParam(hSignHash, HP_HASHVAL, hash, 0)) {
        throwCryptoException(env, "CryptSetHashParam", GetLastError());
        return NULL;
    }
 

    if (!CryptSignHash(hSignHash, AT_KEYEXCHANGE, NULL, 0, NULL, &nsig)) {
        throwCryptoException(env, "CryptSignHash", GetLastError());
        if (GetLastError() != NTE_BAD_LEN) {
            return NULL;
        }
    }

    if ((sig = malloc(nsig)) == NULL) {
        throwException(env, "java/lang/OutOfMemoryError", "sig");
        return NULL;
    }

    if ((csig = malloc(nsig)) == NULL) {
        throwException(env, "java/lang/OutOfMemoryError", "ccig");
        return  NULL;
    }

    if(!CryptSignHash(hSignHash, AT_KEYEXCHANGE, NULL, 0, sig, &nsig)) {
        throwCryptoException(env, "CryptSignHash", GetLastError());
        return NULL;
    }

    // reverse the sig
    for (i = 0; i < nsig; i++) {
        *(csig + i) = *(sig + nsig - i - 1);
    }

    CryptReleaseContext(hSignProv, 0);
    sigblob = (*env)->NewByteArray(env, nsig);
    (*env)->SetByteArrayRegion(env, (jarray)sigblob, 0, nsig, csig);
    free(sig);

    return sigblob;
}

/**
 *
 */
JNIEXPORT jbyteArray JNICALL Java_com_boyter_mscrypto_MSCryptoManager_decryptRSA
 (JNIEnv *env, jobject this_obj, jstring jpadalg, jbyteArray jdata) {

    const jbyte *padalg;
    byte *data;
    byte *encryptblob;
    DWORD ndata;
    jbyteArray decryptblob;
    DWORD blocklen;
    DWORD nblocklen;
    HCRYPTPROV hDecryptProv;
    HCRYPTKEY  hDecryptKey;

//printf("entered function: MSrsaDecrypt   alias: %s\n", alias);
    padalg = (*env)->GetStringUTFChars(env, jpadalg, FALSE);

    if (!CryptAcquireContext(&hDecryptProv, alias, CSP, PROV_RSA_FULL, 0)) {
        throwCryptoException(env, "CryptAcquireContext", GetLastError());
        return NULL;
    }

    if (!CryptGetUserKey(hDecryptProv, AT_KEYEXCHANGE, &hDecryptKey)) {
        throwCryptoException(env, "getting RSA encryption key", GetLastError());
        return NULL;
    }

    ndata = (*env)->GetArrayLength(env, jdata);
    data =  (*env)->GetByteArrayElements(env, jdata, NULL);

    nblocklen = sizeof(DWORD);
    CryptGetKeyParam(hDecryptKey, KP_BLOCKLEN, (char *) &blocklen, &nblocklen, 0);
//printf("MSrsaDecrypt: the rsa block len:  %d bytes\n", blocklen / 8);
//printf("MSrsaDecrypt: the data block len:  %d bytes\n", ndata);

    if (ndata*8 != blocklen) {
        throwCryptoException(env, "invalid data block size", 0L);
        return NULL;
    }

    encryptblob = malloc(ndata);    // reverse the data
    reversecopy(encryptblob, data, ndata);

    if (!CryptDecrypt(hDecryptKey, 0, TRUE, 0, encryptblob, &ndata)) {
        throwCryptoException(env, "%x during CryptDecrypt", GetLastError());
        return NULL;
    }

    decryptblob = (*env)->NewByteArray(env, ndata);
    (*env)->SetByteArrayRegion(env, (jarray) decryptblob, 0, ndata, encryptblob);

    CryptReleaseContext(hDecryptProv, 0);
    return decryptblob;
}

/**
 *
 */
JNIEXPORT jbyteArray JNICALL Java_com_boyter_mscrypto_MSCryptoManager_encryptRSA
(JNIEnv *env, jobject this_obj, jstring jpadalg, jbyteArray jdata) {

    const jbyte *padalg;
    byte *data;
    byte *encryptdata;
    DWORD ndata;
    jbyteArray encryptblob;
    DWORD blocklen;
    DWORD nblocklen;
    byte *buffer;
    HCRYPTPROV hEncryptProv;
    HCRYPTKEY  hEncryptKey;

//printf("entered function:  MSrsaEncrypt   alias: %s\n", alias);
    padalg = (*env)->GetStringUTFChars(env, jpadalg, FALSE);

    if(!CryptAcquireContext(&hEncryptProv, alias, CSP, PROV_RSA_FULL, 0)) {
        throwCryptoException(env, "CryptAcquireContext", GetLastError());
        return NULL;
    }

    if (!CryptGetUserKey(hEncryptProv, AT_KEYEXCHANGE, &hEncryptKey) ) {
        throwCryptoException(env, "RSA encryption key", GetLastError());
        return NULL;
    }

    ndata = (*env)->GetArrayLength(env, jdata);
    data  = (*env)->GetByteArrayElements(env, jdata, NULL);

    nblocklen = sizeof(DWORD);
    CryptGetKeyParam(hEncryptKey, KP_BLOCKLEN, (char *)&blocklen, &nblocklen, 0);
//printf("MSrsaEncrypt: the rsa block len:  %d bytes\n", blocklen/8);
//printf("the data block len:  %d bytes\n", ndata);

    if (ndata * 8 > blocklen) {
        throwCryptoException(env, "invalid data block length", 0L);
        return NULL;
    }

    buffer = malloc(ndata + 1024);
    memcpy(buffer, data, ndata);

    if (!CryptEncrypt(hEncryptKey, 0, TRUE, 0, buffer, &ndata, ndata + 1024)) {
        throwCryptoException(env, "CryptEncrypt", GetLastError());
        return NULL;
    }

    encryptdata = malloc(ndata);    // reverse the data
    reversecopy(encryptdata, buffer, ndata);

    encryptblob = (*env)->NewByteArray(env, ndata);
    (*env)->SetByteArrayRegion(env, (jarray)encryptblob, 0, ndata, encryptdata);

    CryptReleaseContext(hEncryptProv, 0);
    free(buffer);
    return encryptblob;
}

/**
 * MSCryptoManager#getRSAKeysize
 * @return block length
 */
JNIEXPORT jint JNICALL Java_com_boyter_mscrypto_MSCryptoManager_getRSAKeysize
 (JNIEnv *env, jobject this_obj) {

    DWORD blocklen;
    DWORD nblocklen;
    HCRYPTPROV hEncryptProv;
    HCRYPTKEY  hEncryptKey;
    jint jblocklen;

//printf("entered function:  MSrsaGetKeysize   alias: %s\n", alias);

    if (!CryptAcquireContext(&hEncryptProv, alias, CSP, PROV_RSA_FULL, 0)) {
        throwCryptoException(env, "CryptAcquireContext", GetLastError());
        return 0;
    }

    if (! CryptGetUserKey(hEncryptProv, AT_KEYEXCHANGE, &hEncryptKey) ) {
        throwCryptoException(env, "getting RSA encryption key", GetLastError());
        return 0;
    }

    nblocklen = sizeof(DWORD);
    CryptGetKeyParam(hEncryptKey, KP_BLOCKLEN, (char *)&blocklen, &nblocklen, 0);
//printf("MSrsaGetKeysize: the rsa block len:  %d\n", blocklen);

    CryptReleaseContext(hEncryptProv, 0);
    jblocklen = blocklen;
    return jblocklen;
}

/**
 * MSCryptoManager#getCRL
 */
JNIEXPORT jboolean JNICALL Java_com_boyter_mscrypto_MSCryptoManager_getCRL
 (JNIEnv *env, jobject this_obj, jstring jurl) {
    //
    // this function is only valid for Win2K or greater.
    //
#if(WINVER >= 0x0500)
    char *url;
    CRL_CONTEXT *crl=NULL;
    DWORD timeout=120;
    DWORD starttime;
    DWORD endtime;
    double elapsedtime;

    url = (char *) (*env)->GetStringUTFChars(env, jurl, FALSE);

fprintf(stderr, "getCRL: entered URL: %s\n", url);
fflush(stderr);
    starttime = GetTickCount();

    if (!CryptRetrieveObjectByUrl(url, CONTEXT_OID_CRL, 0, timeout * 1000, (LPVOID) &crl, NULL, NULL, NULL, NULL)) {
        throwException(env, "CryptRetrieveObjectByUrl failed", GetLastError());
        endtime = GetTickCount();
        elapsedtime = (float)(endtime-starttime)/1000.0;
fprintf(stderr, "elapsed time %f seconds\n", elapsedtime);
fflush(stderr);
        DeleteUrlCacheEntry(url);    // cached url is probably corrupt
        return JNI_FALSE;
    }

    endtime = GetTickCount();
    elapsedtime = (double)(endtime-starttime)/1000.0;
//printf("elapsed time %f seconds\n", elapsedtime);
//printf("return from MSgetCRL:  crl found\n");

#endif // WINVER >= 0x0500

    return JNI_TRUE;
}

/**
 * throw Crypto exception
 */
static void throwCryptoException(JNIEnv *env, char *gripe, DWORD errnum) {
    char *msg;

//fprintf(stderr, "errno: %d\n", (int) errnum);
//fflush(stderr);

    if (errnum == 0) {
        msg = strdup(gripe);
    } else {
        msg = malloc(strlen(gripe) + 128);
        sprintf(msg, "%s (0x%x)", gripe, (unsigned int) errnum);
    }

//fprintf(stderr, "\n\n********* ERROR in library MSCryptoFunctions *********\n");
//fprintf(stderr, msg);
//fprintf(stderr, "\n\n");
//fflush(stderr);

    throwException(env, "com/boyter/mscrypto/MSCryptoException", msg);
}

/**
 * throw Java exception
 */
static void throwException(JNIEnv *env, char* className, char* message) {
    jclass class = (*env)->FindClass(env, className);
    (*env)->ThrowNew(env, class, message);
}

/**
 * helper function
 * appends an entry to a linked list
 */
static void AddDataToList(list, data, datalength)
List *list;
BYTE *data;
int datalength;
{
    ListEntry *lp;

    lp = malloc(sizeof(ListEntry));
    if (list->first == NULL) {
        list->first = lp;
    }
    if (list->last  != NULL) {
        list->last->next = (char *) lp;
    }
    list->last = lp;

    lp->data = data;
    lp->nbytes = datalength;
    lp->next = NULL;
}

/**
 * base64 encode a block of data
 */
static char base64encode[] = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
    'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
    'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
    'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
};
 
/**
 *
 */
static void base64(x, nbytesin, c, nbytesout)
BYTE *x;
int nbytesin;
char *c;
int nbytesout;
{

    char c1, c2, c3, c4;
    BYTE b1, b2, b3, b4;
    BYTE a1, a2, a3;
    char *cout;
    int i;

    cout = c;
    for (i = 0; i < nbytesin; i+= 3) {
        a1 = *(x + i);
        if (i + 1 < nbytesin) {
            a2 = *(x + i + 1);
        } else {
            a2 = 0;
        }

        if (i + 2 < nbytesin) {
            a3 = *(x + i + 2);
        } else {
            a3 = 0;
        }
        b1 = a1>>2;
        b2 = (a1<<4 | a2>>4) & 0x3f;
        b3 = (a2<<2 | a3>>6) & 0x3f;
        b4 = a3 & 0x3f;
        c1 = base64encode[b1];
        c2 = base64encode[b2];
        c3 = base64encode[b3];
        c4 = base64encode[b4];
        if (i + 1 >= nbytesin) {
            c3 = '=';
        }
        if (i + 2 >= nbytesin) {
            c4 = '=';
        }

        *cout = c1;    cout++;
        *cout = c2;    cout++;
        *cout = c3;    cout++;
        *cout = c4;    cout++;
    }
    *(c + nbytesout) = '\0';
//printf("\nbase64-encoded cert\n%s\n", c);
}

/**
 * helper function
 * copies block of bytes in reverse order
 */
static void reversecopy(byte *dest, byte *src, int n) {
    int i;
    for (i = 0; i < n; i++) {
        *(dest + i) = *(src + n - i - 1);
    }
}

/**
 * helper function
 * convert a Unicode String to an ANSI string
 */
static char *strconvert(wchar_t *w) {
    int len;
    char *s;
    int i;

    len = wcslen(w);
    s = malloc(len + 1);
    for (i = 0; i < len; i++) {
        *(s + i) = (char) *(w + i);
    }
    *(s + len) = '\0';

    return s;
}

/* */
