package com.keanbin.pinyinime;

interface IPinyinDecoderService {
    int getInt();
    void setMaxLens(int maxSpsLen, int maxHzsLen);
    int imSearch(in byte[] pyBuf, int pyLen);
    int imDelSearch(int pos, boolean is_pos_in_splid, boolean clear_fixed_this_step);
    void imResetSearch();
    int imAddLetter(byte ch);
    String imGetPyStr(boolean decoded);
    int imGetPyStrLen(boolean decoded);
    int[] imGetSplStart();
    String imGetChoice(int choiceId);
    String imGetChoices(int choicesNum);
    List<String> imGetChoiceList(int choicesStart, int choicesNum, int sentFixedLen);
    int imChoose(int choiceId);
    int imCancelLastChoice();
    int imGetFixedLen();
    boolean imCancelInput();
    void imFlushCache();
    int imGetPredictsNum(in String fixedStr);
    List<String> imGetPredictList(int predictsStart, int predictsNum);
    String imGetPredictItem(int predictNo);

    String syncUserDict(in String tomerge);
    boolean syncBegin();
    void syncFinish();
    int syncPutLemmas(in String tomerge);
    String syncGetLemmas();
    int syncGetLastCount();
    int syncGetTotalCount();
    void syncClearLastGot();
    int imSyncGetCapacity();
}
