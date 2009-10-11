#define CERT_SYSTEM_STORE_CURRENT_USER 0x10000
#define CRYPT_E_NO_REVOCATION_CHECK 0x80092012 // 失効の関数は証明書の失効を確認できませんでした。
#define CRYPT_E_NO_REVOCATION_DLL 0x80092011 // 失効を確認する DLL またはエクスポートされた関数が見つかりませんでした。
#define CRYPT_E_NOT_IN_REVOCATION_DATABASE 0x80092014 // この証明書は失効サーバーのデータベースにありません。
#define CERT_KEY_PROV_INFO_PROP_ID 2
typedef struct _CERT_REVOCATION_STATUS {
  DWORD cbSize;
  DWORD dwIndex;
  DWORD dwError;
  DWORD dwReason;
  BOOL fHasFreshnessTime;
  DWORD dwFreshnessTime;
} CERT_REVOCATION_STATUS, *PCERT_REVOCATION_STATUS;
#define CERT_CONTEXT_REVOCATION_TYPE 1
#define CERT_NAME_RDN_TYPE 2
#define CERT_NAME_SIMPLE_DISPLAY_TYPE 4
#define CRYPT_ACQUIRE_CACHE_FLAG 0x1
typedef struct _CRYPT_KEY_PROV_PARAM {
        DWORD dwParam;
        BYTE *pbData;
        DWORD cbData;
        DWORD dwFlags;
} CRYPT_KEY_PROV_PARAM,*PCRYPT_KEY_PROV_PARAM;typedef struct _CRYPT_KEY_PROV_INFO {
        LPWSTR pwszContainerName;
        LPWSTR pwszProvName;
        DWORD dwProvType;
        DWORD dwFlags;
        DWORD cProvParam;
        PCRYPT_KEY_PROV_PARAM rgProvParam;
        DWORD dwKeySpec;
} CRYPT_KEY_PROV_INFO,*PCRYPT_KEY_PROV_INFO;
#define CERT_NAME_SIMPLE_DISPLAY_TYPE 4
PCCERT_CONTEXT __declspec(dllimport) WINAPI CertEnumCertificatesInStore(HCERTSTORE, PCCERT_CONTEXT);
PCCERT_CONTEXT __declspec(dllimport) WINAPI CertCreateCertificateContext(DWORD, const BYTE*, DWORD);
DWORD __declspec(dllimport) WINAPI CertGetNameStringA(
	PCCERT_CONTEXT pCertContext,
        DWORD dwType,
        DWORD dwFlags,
        void *pvTypePara,
        LPSTR pszNameString,
        DWORD cchNameString
);
DWORD __declspec(dllimport) WINAPI CertGetNameStringW(
	PCCERT_CONTEXT pCertContext,
        DWORD dwType,
        DWORD dwFlags,
        void *pvTypePara,
        LPWSTR pszNameString,
        DWORD cchNameString
);
#ifdef UNICODE
#define CertGetNameString CertGetNameStringW
#else
#define CertGetNameString CertGetNameStringA
#endif
typedef struct _CERT_REVOCATION_PARA {
        DWORD cbSize;
        PCCERT_CONTEXT pIssuerCert;
        DWORD cCertStore;
        HCERTSTORE *rgCertStore;
} CERT_REVOCATION_PARA,*PCERT_REVOCATION_PARA;
BOOL __declspec(dllimport) WINAPI CertVerifyRevocation(
	DWORD dwEncodingType,
	DWORD dwRevType,
	DWORD cContext,
	PVOID rgpvContext[],
	DWORD dwFlags,
	PCERT_REVOCATION_PARA pRevPara,
	PCERT_REVOCATION_STATUS pRevStatus);
BOOL __declspec(dllimport) WINAPI CryptAcquireCertificatePrivateKey(
	PCCERT_CONTEXT pCert,
	DWORD dwFlags,
	void *pvReserved,
	HCRYPTPROV * phCryptProv,
	DWORD * pdwKeySpec,
	BOOL * pfCallerFreeProv);
BOOL __declspec(dllimport) WINAPI CertGetCertificateContextProperty(PCCERT_CONTEXT, DWORD, void*, DWORD*);
