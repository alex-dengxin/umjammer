/*
 *  X68000 Emulator in Java
 *
 *  Copyright (C) 2003,2004 by M.Kamada
 */

package vavi.apps.x68k;


class TextScreen extends MemoryMappedDevice implements X68000Device {
    private boolean simultaneous_access;
    private boolean bit_mask;
    private int copy_plane;
    private int access_plane;
    private int mask_even;
    private int mask_odd;
    private int source_raster_offset;
    private int destination_raster_offset;
    public int scroll_x;
    public int scroll_y;
    public int scroll_offset_x;
    public int scroll_offset_y;
    public int scroll_shift;

    private X68000 x68000;

    private CRTC crtc;

    public TextScreen() {
    }

    public boolean init(X68000 x68000) {
        this.x68000 = x68000;
        crtc = x68000.crtc;
        reset();
        return true;
    }

    public void reset() {
        simultaneous_access = false;
        bit_mask = false;
        copy_plane = 0;
        access_plane = 0;
        mask_even = 0;
        mask_odd = 0;
        source_raster_offset = 0;
        destination_raster_offset = 0;
        scroll_x = 0;
        scroll_y = 0;
        scroll_offset_x = 0;
        scroll_offset_y = 0;
        scroll_shift = 0;
    }

    public void setMode(int mode) {
        simultaneous_access = (mode & 1) != 0;
        bit_mask = (mode & 2) != 0;
    }

    public void setPlane(int plane) {
        copy_plane = plane & 15;
        access_plane = plane >> 4 & 15;
    }

    public void setMaskHigh(int mask) {
        mask_even = mask & 255;
    }

    public void setMaskLow(int mask) {
        mask_odd = mask & 255;
    }

    public void setSourceRaster(int raster) {
        source_raster_offset = (raster & 255) << 9;
    }

    public void setDestinationRaster(int raster) {
        destination_raster_offset = (raster & 255) << 9;
    }

    public void setScrollX(int x) {
        scroll_x = x & 1023;
        scroll_offset_x = scroll_x >> 3;
        scroll_shift = scroll_x & 7;
        crtc.updateAll();
    }

    public void setScrollY(int y) {
        scroll_y = y & 1023;
        scroll_offset_y = scroll_y << 7;
        crtc.updateAll();
    }

    public void rasterCopy() {
        for (int p = copy_plane, a = 14680064 + source_raster_offset; a < 15204352; p >>= 1, a += 131072) {
            if ((p & 1) != 0) {
                int b = a - source_raster_offset + destination_raster_offset;
                m[b] = m[a];
                m[b + 1] = m[a + 1];
                m[b + 2] = m[a + 2];
                m[b + 3] = m[a + 3];
                m[b + 4] = m[a + 4];
                m[b + 5] = m[a + 5];
                m[b + 6] = m[a + 6];
                m[b + 7] = m[a + 7];
                m[b + 8] = m[a + 8];
                m[b + 9] = m[a + 9];
                m[b + 10] = m[a + 10];
                m[b + 11] = m[a + 11];
                m[b + 12] = m[a + 12];
                m[b + 13] = m[a + 13];
                m[b + 14] = m[a + 14];
                m[b + 15] = m[a + 15];
                m[b + 16] = m[a + 16];
                m[b + 17] = m[a + 17];
                m[b + 18] = m[a + 18];
                m[b + 19] = m[a + 19];
                m[b + 20] = m[a + 20];
                m[b + 21] = m[a + 21];
                m[b + 22] = m[a + 22];
                m[b + 23] = m[a + 23];
                m[b + 24] = m[a + 24];
                m[b + 25] = m[a + 25];
                m[b + 26] = m[a + 26];
                m[b + 27] = m[a + 27];
                m[b + 28] = m[a + 28];
                m[b + 29] = m[a + 29];
                m[b + 30] = m[a + 30];
                m[b + 31] = m[a + 31];
                m[b + 32] = m[a + 32];
                m[b + 33] = m[a + 33];
                m[b + 34] = m[a + 34];
                m[b + 35] = m[a + 35];
                m[b + 36] = m[a + 36];
                m[b + 37] = m[a + 37];
                m[b + 38] = m[a + 38];
                m[b + 39] = m[a + 39];
                m[b + 40] = m[a + 40];
                m[b + 41] = m[a + 41];
                m[b + 42] = m[a + 42];
                m[b + 43] = m[a + 43];
                m[b + 44] = m[a + 44];
                m[b + 45] = m[a + 45];
                m[b + 46] = m[a + 46];
                m[b + 47] = m[a + 47];
                m[b + 48] = m[a + 48];
                m[b + 49] = m[a + 49];
                m[b + 50] = m[a + 50];
                m[b + 51] = m[a + 51];
                m[b + 52] = m[a + 52];
                m[b + 53] = m[a + 53];
                m[b + 54] = m[a + 54];
                m[b + 55] = m[a + 55];
                m[b + 56] = m[a + 56];
                m[b + 57] = m[a + 57];
                m[b + 58] = m[a + 58];
                m[b + 59] = m[a + 59];
                m[b + 60] = m[a + 60];
                m[b + 61] = m[a + 61];
                m[b + 62] = m[a + 62];
                m[b + 63] = m[a + 63];
                m[b + 64] = m[a + 64];
                m[b + 65] = m[a + 65];
                m[b + 66] = m[a + 66];
                m[b + 67] = m[a + 67];
                m[b + 68] = m[a + 68];
                m[b + 69] = m[a + 69];
                m[b + 70] = m[a + 70];
                m[b + 71] = m[a + 71];
                m[b + 72] = m[a + 72];
                m[b + 73] = m[a + 73];
                m[b + 74] = m[a + 74];
                m[b + 75] = m[a + 75];
                m[b + 76] = m[a + 76];
                m[b + 77] = m[a + 77];
                m[b + 78] = m[a + 78];
                m[b + 79] = m[a + 79];
                m[b + 80] = m[a + 80];
                m[b + 81] = m[a + 81];
                m[b + 82] = m[a + 82];
                m[b + 83] = m[a + 83];
                m[b + 84] = m[a + 84];
                m[b + 85] = m[a + 85];
                m[b + 86] = m[a + 86];
                m[b + 87] = m[a + 87];
                m[b + 88] = m[a + 88];
                m[b + 89] = m[a + 89];
                m[b + 90] = m[a + 90];
                m[b + 91] = m[a + 91];
                m[b + 92] = m[a + 92];
                m[b + 93] = m[a + 93];
                m[b + 94] = m[a + 94];
                m[b + 95] = m[a + 95];
                m[b + 96] = m[a + 96];
                m[b + 97] = m[a + 97];
                m[b + 98] = m[a + 98];
                m[b + 99] = m[a + 99];
                m[b + 100] = m[a + 100];
                m[b + 101] = m[a + 101];
                m[b + 102] = m[a + 102];
                m[b + 103] = m[a + 103];
                m[b + 104] = m[a + 104];
                m[b + 105] = m[a + 105];
                m[b + 106] = m[a + 106];
                m[b + 107] = m[a + 107];
                m[b + 108] = m[a + 108];
                m[b + 109] = m[a + 109];
                m[b + 110] = m[a + 110];
                m[b + 111] = m[a + 111];
                m[b + 112] = m[a + 112];
                m[b + 113] = m[a + 113];
                m[b + 114] = m[a + 114];
                m[b + 115] = m[a + 115];
                m[b + 116] = m[a + 116];
                m[b + 117] = m[a + 117];
                m[b + 118] = m[a + 118];
                m[b + 119] = m[a + 119];
                m[b + 120] = m[a + 120];
                m[b + 121] = m[a + 121];
                m[b + 122] = m[a + 122];
                m[b + 123] = m[a + 123];
                m[b + 124] = m[a + 124];
                m[b + 125] = m[a + 125];
                m[b + 126] = m[a + 126];
                m[b + 127] = m[a + 127];
                m[b + 128] = m[a + 128];
                m[b + 129] = m[a + 129];
                m[b + 130] = m[a + 130];
                m[b + 131] = m[a + 131];
                m[b + 132] = m[a + 132];
                m[b + 133] = m[a + 133];
                m[b + 134] = m[a + 134];
                m[b + 135] = m[a + 135];
                m[b + 136] = m[a + 136];
                m[b + 137] = m[a + 137];
                m[b + 138] = m[a + 138];
                m[b + 139] = m[a + 139];
                m[b + 140] = m[a + 140];
                m[b + 141] = m[a + 141];
                m[b + 142] = m[a + 142];
                m[b + 143] = m[a + 143];
                m[b + 144] = m[a + 144];
                m[b + 145] = m[a + 145];
                m[b + 146] = m[a + 146];
                m[b + 147] = m[a + 147];
                m[b + 148] = m[a + 148];
                m[b + 149] = m[a + 149];
                m[b + 150] = m[a + 150];
                m[b + 151] = m[a + 151];
                m[b + 152] = m[a + 152];
                m[b + 153] = m[a + 153];
                m[b + 154] = m[a + 154];
                m[b + 155] = m[a + 155];
                m[b + 156] = m[a + 156];
                m[b + 157] = m[a + 157];
                m[b + 158] = m[a + 158];
                m[b + 159] = m[a + 159];
                m[b + 160] = m[a + 160];
                m[b + 161] = m[a + 161];
                m[b + 162] = m[a + 162];
                m[b + 163] = m[a + 163];
                m[b + 164] = m[a + 164];
                m[b + 165] = m[a + 165];
                m[b + 166] = m[a + 166];
                m[b + 167] = m[a + 167];
                m[b + 168] = m[a + 168];
                m[b + 169] = m[a + 169];
                m[b + 170] = m[a + 170];
                m[b + 171] = m[a + 171];
                m[b + 172] = m[a + 172];
                m[b + 173] = m[a + 173];
                m[b + 174] = m[a + 174];
                m[b + 175] = m[a + 175];
                m[b + 176] = m[a + 176];
                m[b + 177] = m[a + 177];
                m[b + 178] = m[a + 178];
                m[b + 179] = m[a + 179];
                m[b + 180] = m[a + 180];
                m[b + 181] = m[a + 181];
                m[b + 182] = m[a + 182];
                m[b + 183] = m[a + 183];
                m[b + 184] = m[a + 184];
                m[b + 185] = m[a + 185];
                m[b + 186] = m[a + 186];
                m[b + 187] = m[a + 187];
                m[b + 188] = m[a + 188];
                m[b + 189] = m[a + 189];
                m[b + 190] = m[a + 190];
                m[b + 191] = m[a + 191];
                m[b + 192] = m[a + 192];
                m[b + 193] = m[a + 193];
                m[b + 194] = m[a + 194];
                m[b + 195] = m[a + 195];
                m[b + 196] = m[a + 196];
                m[b + 197] = m[a + 197];
                m[b + 198] = m[a + 198];
                m[b + 199] = m[a + 199];
                m[b + 200] = m[a + 200];
                m[b + 201] = m[a + 201];
                m[b + 202] = m[a + 202];
                m[b + 203] = m[a + 203];
                m[b + 204] = m[a + 204];
                m[b + 205] = m[a + 205];
                m[b + 206] = m[a + 206];
                m[b + 207] = m[a + 207];
                m[b + 208] = m[a + 208];
                m[b + 209] = m[a + 209];
                m[b + 210] = m[a + 210];
                m[b + 211] = m[a + 211];
                m[b + 212] = m[a + 212];
                m[b + 213] = m[a + 213];
                m[b + 214] = m[a + 214];
                m[b + 215] = m[a + 215];
                m[b + 216] = m[a + 216];
                m[b + 217] = m[a + 217];
                m[b + 218] = m[a + 218];
                m[b + 219] = m[a + 219];
                m[b + 220] = m[a + 220];
                m[b + 221] = m[a + 221];
                m[b + 222] = m[a + 222];
                m[b + 223] = m[a + 223];
                m[b + 224] = m[a + 224];
                m[b + 225] = m[a + 225];
                m[b + 226] = m[a + 226];
                m[b + 227] = m[a + 227];
                m[b + 228] = m[a + 228];
                m[b + 229] = m[a + 229];
                m[b + 230] = m[a + 230];
                m[b + 231] = m[a + 231];
                m[b + 232] = m[a + 232];
                m[b + 233] = m[a + 233];
                m[b + 234] = m[a + 234];
                m[b + 235] = m[a + 235];
                m[b + 236] = m[a + 236];
                m[b + 237] = m[a + 237];
                m[b + 238] = m[a + 238];
                m[b + 239] = m[a + 239];
                m[b + 240] = m[a + 240];
                m[b + 241] = m[a + 241];
                m[b + 242] = m[a + 242];
                m[b + 243] = m[a + 243];
                m[b + 244] = m[a + 244];
                m[b + 245] = m[a + 245];
                m[b + 246] = m[a + 246];
                m[b + 247] = m[a + 247];
                m[b + 248] = m[a + 248];
                m[b + 249] = m[a + 249];
                m[b + 250] = m[a + 250];
                m[b + 251] = m[a + 251];
                m[b + 252] = m[a + 252];
                m[b + 253] = m[a + 253];
                m[b + 254] = m[a + 254];
                m[b + 255] = m[a + 255];
                m[b + 256] = m[a + 256];
                m[b + 257] = m[a + 257];
                m[b + 258] = m[a + 258];
                m[b + 259] = m[a + 259];
                m[b + 260] = m[a + 260];
                m[b + 261] = m[a + 261];
                m[b + 262] = m[a + 262];
                m[b + 263] = m[a + 263];
                m[b + 264] = m[a + 264];
                m[b + 265] = m[a + 265];
                m[b + 266] = m[a + 266];
                m[b + 267] = m[a + 267];
                m[b + 268] = m[a + 268];
                m[b + 269] = m[a + 269];
                m[b + 270] = m[a + 270];
                m[b + 271] = m[a + 271];
                m[b + 272] = m[a + 272];
                m[b + 273] = m[a + 273];
                m[b + 274] = m[a + 274];
                m[b + 275] = m[a + 275];
                m[b + 276] = m[a + 276];
                m[b + 277] = m[a + 277];
                m[b + 278] = m[a + 278];
                m[b + 279] = m[a + 279];
                m[b + 280] = m[a + 280];
                m[b + 281] = m[a + 281];
                m[b + 282] = m[a + 282];
                m[b + 283] = m[a + 283];
                m[b + 284] = m[a + 284];
                m[b + 285] = m[a + 285];
                m[b + 286] = m[a + 286];
                m[b + 287] = m[a + 287];
                m[b + 288] = m[a + 288];
                m[b + 289] = m[a + 289];
                m[b + 290] = m[a + 290];
                m[b + 291] = m[a + 291];
                m[b + 292] = m[a + 292];
                m[b + 293] = m[a + 293];
                m[b + 294] = m[a + 294];
                m[b + 295] = m[a + 295];
                m[b + 296] = m[a + 296];
                m[b + 297] = m[a + 297];
                m[b + 298] = m[a + 298];
                m[b + 299] = m[a + 299];
                m[b + 300] = m[a + 300];
                m[b + 301] = m[a + 301];
                m[b + 302] = m[a + 302];
                m[b + 303] = m[a + 303];
                m[b + 304] = m[a + 304];
                m[b + 305] = m[a + 305];
                m[b + 306] = m[a + 306];
                m[b + 307] = m[a + 307];
                m[b + 308] = m[a + 308];
                m[b + 309] = m[a + 309];
                m[b + 310] = m[a + 310];
                m[b + 311] = m[a + 311];
                m[b + 312] = m[a + 312];
                m[b + 313] = m[a + 313];
                m[b + 314] = m[a + 314];
                m[b + 315] = m[a + 315];
                m[b + 316] = m[a + 316];
                m[b + 317] = m[a + 317];
                m[b + 318] = m[a + 318];
                m[b + 319] = m[a + 319];
                m[b + 320] = m[a + 320];
                m[b + 321] = m[a + 321];
                m[b + 322] = m[a + 322];
                m[b + 323] = m[a + 323];
                m[b + 324] = m[a + 324];
                m[b + 325] = m[a + 325];
                m[b + 326] = m[a + 326];
                m[b + 327] = m[a + 327];
                m[b + 328] = m[a + 328];
                m[b + 329] = m[a + 329];
                m[b + 330] = m[a + 330];
                m[b + 331] = m[a + 331];
                m[b + 332] = m[a + 332];
                m[b + 333] = m[a + 333];
                m[b + 334] = m[a + 334];
                m[b + 335] = m[a + 335];
                m[b + 336] = m[a + 336];
                m[b + 337] = m[a + 337];
                m[b + 338] = m[a + 338];
                m[b + 339] = m[a + 339];
                m[b + 340] = m[a + 340];
                m[b + 341] = m[a + 341];
                m[b + 342] = m[a + 342];
                m[b + 343] = m[a + 343];
                m[b + 344] = m[a + 344];
                m[b + 345] = m[a + 345];
                m[b + 346] = m[a + 346];
                m[b + 347] = m[a + 347];
                m[b + 348] = m[a + 348];
                m[b + 349] = m[a + 349];
                m[b + 350] = m[a + 350];
                m[b + 351] = m[a + 351];
                m[b + 352] = m[a + 352];
                m[b + 353] = m[a + 353];
                m[b + 354] = m[a + 354];
                m[b + 355] = m[a + 355];
                m[b + 356] = m[a + 356];
                m[b + 357] = m[a + 357];
                m[b + 358] = m[a + 358];
                m[b + 359] = m[a + 359];
                m[b + 360] = m[a + 360];
                m[b + 361] = m[a + 361];
                m[b + 362] = m[a + 362];
                m[b + 363] = m[a + 363];
                m[b + 364] = m[a + 364];
                m[b + 365] = m[a + 365];
                m[b + 366] = m[a + 366];
                m[b + 367] = m[a + 367];
                m[b + 368] = m[a + 368];
                m[b + 369] = m[a + 369];
                m[b + 370] = m[a + 370];
                m[b + 371] = m[a + 371];
                m[b + 372] = m[a + 372];
                m[b + 373] = m[a + 373];
                m[b + 374] = m[a + 374];
                m[b + 375] = m[a + 375];
                m[b + 376] = m[a + 376];
                m[b + 377] = m[a + 377];
                m[b + 378] = m[a + 378];
                m[b + 379] = m[a + 379];
                m[b + 380] = m[a + 380];
                m[b + 381] = m[a + 381];
                m[b + 382] = m[a + 382];
                m[b + 383] = m[a + 383];
                m[b + 384] = m[a + 384];
                m[b + 385] = m[a + 385];
                m[b + 386] = m[a + 386];
                m[b + 387] = m[a + 387];
                m[b + 388] = m[a + 388];
                m[b + 389] = m[a + 389];
                m[b + 390] = m[a + 390];
                m[b + 391] = m[a + 391];
                m[b + 392] = m[a + 392];
                m[b + 393] = m[a + 393];
                m[b + 394] = m[a + 394];
                m[b + 395] = m[a + 395];
                m[b + 396] = m[a + 396];
                m[b + 397] = m[a + 397];
                m[b + 398] = m[a + 398];
                m[b + 399] = m[a + 399];
                m[b + 400] = m[a + 400];
                m[b + 401] = m[a + 401];
                m[b + 402] = m[a + 402];
                m[b + 403] = m[a + 403];
                m[b + 404] = m[a + 404];
                m[b + 405] = m[a + 405];
                m[b + 406] = m[a + 406];
                m[b + 407] = m[a + 407];
                m[b + 408] = m[a + 408];
                m[b + 409] = m[a + 409];
                m[b + 410] = m[a + 410];
                m[b + 411] = m[a + 411];
                m[b + 412] = m[a + 412];
                m[b + 413] = m[a + 413];
                m[b + 414] = m[a + 414];
                m[b + 415] = m[a + 415];
                m[b + 416] = m[a + 416];
                m[b + 417] = m[a + 417];
                m[b + 418] = m[a + 418];
                m[b + 419] = m[a + 419];
                m[b + 420] = m[a + 420];
                m[b + 421] = m[a + 421];
                m[b + 422] = m[a + 422];
                m[b + 423] = m[a + 423];
                m[b + 424] = m[a + 424];
                m[b + 425] = m[a + 425];
                m[b + 426] = m[a + 426];
                m[b + 427] = m[a + 427];
                m[b + 428] = m[a + 428];
                m[b + 429] = m[a + 429];
                m[b + 430] = m[a + 430];
                m[b + 431] = m[a + 431];
                m[b + 432] = m[a + 432];
                m[b + 433] = m[a + 433];
                m[b + 434] = m[a + 434];
                m[b + 435] = m[a + 435];
                m[b + 436] = m[a + 436];
                m[b + 437] = m[a + 437];
                m[b + 438] = m[a + 438];
                m[b + 439] = m[a + 439];
                m[b + 440] = m[a + 440];
                m[b + 441] = m[a + 441];
                m[b + 442] = m[a + 442];
                m[b + 443] = m[a + 443];
                m[b + 444] = m[a + 444];
                m[b + 445] = m[a + 445];
                m[b + 446] = m[a + 446];
                m[b + 447] = m[a + 447];
                m[b + 448] = m[a + 448];
                m[b + 449] = m[a + 449];
                m[b + 450] = m[a + 450];
                m[b + 451] = m[a + 451];
                m[b + 452] = m[a + 452];
                m[b + 453] = m[a + 453];
                m[b + 454] = m[a + 454];
                m[b + 455] = m[a + 455];
                m[b + 456] = m[a + 456];
                m[b + 457] = m[a + 457];
                m[b + 458] = m[a + 458];
                m[b + 459] = m[a + 459];
                m[b + 460] = m[a + 460];
                m[b + 461] = m[a + 461];
                m[b + 462] = m[a + 462];
                m[b + 463] = m[a + 463];
                m[b + 464] = m[a + 464];
                m[b + 465] = m[a + 465];
                m[b + 466] = m[a + 466];
                m[b + 467] = m[a + 467];
                m[b + 468] = m[a + 468];
                m[b + 469] = m[a + 469];
                m[b + 470] = m[a + 470];
                m[b + 471] = m[a + 471];
                m[b + 472] = m[a + 472];
                m[b + 473] = m[a + 473];
                m[b + 474] = m[a + 474];
                m[b + 475] = m[a + 475];
                m[b + 476] = m[a + 476];
                m[b + 477] = m[a + 477];
                m[b + 478] = m[a + 478];
                m[b + 479] = m[a + 479];
                m[b + 480] = m[a + 480];
                m[b + 481] = m[a + 481];
                m[b + 482] = m[a + 482];
                m[b + 483] = m[a + 483];
                m[b + 484] = m[a + 484];
                m[b + 485] = m[a + 485];
                m[b + 486] = m[a + 486];
                m[b + 487] = m[a + 487];
                m[b + 488] = m[a + 488];
                m[b + 489] = m[a + 489];
                m[b + 490] = m[a + 490];
                m[b + 491] = m[a + 491];
                m[b + 492] = m[a + 492];
                m[b + 493] = m[a + 493];
                m[b + 494] = m[a + 494];
                m[b + 495] = m[a + 495];
                m[b + 496] = m[a + 496];
                m[b + 497] = m[a + 497];
                m[b + 498] = m[a + 498];
                m[b + 499] = m[a + 499];
                m[b + 500] = m[a + 500];
                m[b + 501] = m[a + 501];
                m[b + 502] = m[a + 502];
                m[b + 503] = m[a + 503];
                m[b + 504] = m[a + 504];
                m[b + 505] = m[a + 505];
                m[b + 506] = m[a + 506];
                m[b + 507] = m[a + 507];
                m[b + 508] = m[a + 508];
                m[b + 509] = m[a + 509];
                m[b + 510] = m[a + 510];
                m[b + 511] = m[a + 511];
            }
        }
        int y = (destination_raster_offset >> 7) - scroll_y;
        crtc.sc[y & 1023]++;
        crtc.sc[y + 1 & 1023]++;
        crtc.sc[y + 2 & 1023]++;
        crtc.sc[y + 3 & 1023]++;
    }

    public void write_byte(int a, byte b) {
        if (simultaneous_access) {
            a &= 131071;
            if (bit_mask) {
                int mask = (a & 1) == 0 ? mask_even : mask_odd;
                b &= ~mask;
                if ((access_plane & 1) != 0) {
                    m[14680064 + a] = (byte) ((m[14680064 + a] & mask) + b);
                }
                if ((access_plane & 2) != 0) {
                    m[14811136 + a] = (byte) ((m[14811136 + a] & mask) + b);
                }
                if ((access_plane & 4) != 0) {
                    m[14942208 + a] = (byte) ((m[14942208 + a] & mask) + b);
                }
                if ((access_plane & 8) != 0) {
                    m[15073280 + a] = (byte) ((m[15073280 + a] & mask) + b);
                }
            } else {
                if ((access_plane & 1) != 0) {
                    m[14680064 + a] = b;
                }
                if ((access_plane & 2) != 0) {
                    m[14811136 + a] = b;
                }
                if ((access_plane & 4) != 0) {
                    m[14942208 + a] = b;
                }
                if ((access_plane & 8) != 0) {
                    m[15073280 + a] = b;
                }
            }
        } else if (bit_mask) {
            int mask = (a & 1) == 0 ? mask_even : mask_odd;
            m[a] = (byte) ((m[a] & mask) + (b & ~mask));
        } else {
            m[a] = b;
        }
        crtc.sc[(a >> 7) - scroll_y & 1023]++;
    }

    public void write_short_big(int a, short s) {
        int h = s >> 8;
        if (simultaneous_access) {
            a &= 131071;
            if (bit_mask) {
                h &= ~mask_even;
                s &= ~mask_odd;
                if ((access_plane & 1) != 0) {
                    m[14680064 + a] = (byte) ((m[14680064 + a] & mask_even) + h);
                    m[14680065 + a] = (byte) ((m[14680065 + a] & mask_odd) + s);
                }
                if ((access_plane & 2) != 0) {
                    m[14811136 + a] = (byte) ((m[14811136 + a] & mask_even) + h);
                    m[14811137 + a] = (byte) ((m[14811137 + a] & mask_odd) + s);
                }
                if ((access_plane & 4) != 0) {
                    m[14942208 + a] = (byte) ((m[14942208 + a] & mask_even) + h);
                    m[14942209 + a] = (byte) ((m[14942209 + a] & mask_odd) + s);
                }
                if ((access_plane & 8) != 0) {
                    m[15073280 + a] = (byte) ((m[15073280 + a] & mask_even) + h);
                    m[15073281 + a] = (byte) ((m[15073281 + a] & mask_odd) + s);
                }
            } else {
                if ((access_plane & 1) != 0) {
                    m[14680064 + a] = (byte) h;
                    m[14680065 + a] = (byte) s;
                }
                if ((access_plane & 2) != 0) {
                    m[14811136 + a] = (byte) h;
                    m[14811137 + a] = (byte) s;
                }
                if ((access_plane & 4) != 0) {
                    m[14942208 + a] = (byte) h;
                    m[14942209 + a] = (byte) s;
                }
                if ((access_plane & 8) != 0) {
                    m[15073280 + a] = (byte) h;
                    m[15073281 + a] = (byte) s;
                }
            }
        } else if (bit_mask) {
            m[a] = (byte) ((m[a] & mask_even) + (h & ~mask_even));
            m[a + 1] = (byte) ((m[a + 1] & mask_odd) + (s & ~mask_odd));
        } else {
            m[a] = (byte) h;
            m[a + 1] = (byte) s;
        }
        crtc.sc[(a >> 7) - scroll_y & 1023]++;
    }

    public void write_int_big(int a, int i) {
        if (simultaneous_access || bit_mask) {
            write_short_big(a, (short) (i >>> 16));
            write_short_big(a + 2, (short) i);
            return;
        }
        m[a] = (byte) (i >>> 24);
        m[a + 1] = (byte) (i >>> 16);
        m[a + 2] = (byte) (i >>> 8);
        m[a + 3] = (byte) i;
        crtc.sc[(a >> 7) - scroll_y & 1023]++;
    }
}

