/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\code\\hht\\pinyinIme\\src\\main\\aidl\\com\\keanbin\\pinyinime\\IPinyinDecoderService.aidl
 */
package com.keanbin.pinyinime;
public interface IPinyinDecoderService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.keanbin.pinyinime.IPinyinDecoderService
{
private static final java.lang.String DESCRIPTOR = "com.keanbin.pinyinime.IPinyinDecoderService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.keanbin.pinyinime.IPinyinDecoderService interface,
 * generating a proxy if needed.
 */
public static com.keanbin.pinyinime.IPinyinDecoderService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.keanbin.pinyinime.IPinyinDecoderService))) {
return ((com.keanbin.pinyinime.IPinyinDecoderService)iin);
}
return new com.keanbin.pinyinime.IPinyinDecoderService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getInt:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getInt();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setMaxLens:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
this.setMaxLens(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_imSearch:
{
data.enforceInterface(DESCRIPTOR);
byte[] _arg0;
_arg0 = data.createByteArray();
int _arg1;
_arg1 = data.readInt();
int _result = this.imSearch(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_imDelSearch:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _arg1;
_arg1 = (0!=data.readInt());
boolean _arg2;
_arg2 = (0!=data.readInt());
int _result = this.imDelSearch(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_imResetSearch:
{
data.enforceInterface(DESCRIPTOR);
this.imResetSearch();
reply.writeNoException();
return true;
}
case TRANSACTION_imAddLetter:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
int _result = this.imAddLetter(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_imGetPyStr:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
java.lang.String _result = this.imGetPyStr(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_imGetPyStrLen:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
int _result = this.imGetPyStrLen(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_imGetSplStart:
{
data.enforceInterface(DESCRIPTOR);
int[] _result = this.imGetSplStart();
reply.writeNoException();
reply.writeIntArray(_result);
return true;
}
case TRANSACTION_imGetChoice:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _result = this.imGetChoice(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_imGetChoices:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _result = this.imGetChoices(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_imGetChoiceList:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
java.util.List<java.lang.String> _result = this.imGetChoiceList(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeStringList(_result);
return true;
}
case TRANSACTION_imChoose:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _result = this.imChoose(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_imCancelLastChoice:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.imCancelLastChoice();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_imGetFixedLen:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.imGetFixedLen();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_imCancelInput:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.imCancelInput();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_imFlushCache:
{
data.enforceInterface(DESCRIPTOR);
this.imFlushCache();
reply.writeNoException();
return true;
}
case TRANSACTION_imGetPredictsNum:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.imGetPredictsNum(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_imGetPredictList:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
java.util.List<java.lang.String> _result = this.imGetPredictList(_arg0, _arg1);
reply.writeNoException();
reply.writeStringList(_result);
return true;
}
case TRANSACTION_imGetPredictItem:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _result = this.imGetPredictItem(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_syncUserDict:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.syncUserDict(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_syncBegin:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.syncBegin();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_syncFinish:
{
data.enforceInterface(DESCRIPTOR);
this.syncFinish();
reply.writeNoException();
return true;
}
case TRANSACTION_syncPutLemmas:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.syncPutLemmas(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_syncGetLemmas:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.syncGetLemmas();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_syncGetLastCount:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.syncGetLastCount();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_syncGetTotalCount:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.syncGetTotalCount();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_syncClearLastGot:
{
data.enforceInterface(DESCRIPTOR);
this.syncClearLastGot();
reply.writeNoException();
return true;
}
case TRANSACTION_imSyncGetCapacity:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.imSyncGetCapacity();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.keanbin.pinyinime.IPinyinDecoderService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public int getInt() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getInt, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setMaxLens(int maxSpsLen, int maxHzsLen) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(maxSpsLen);
_data.writeInt(maxHzsLen);
mRemote.transact(Stub.TRANSACTION_setMaxLens, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int imSearch(byte[] pyBuf, int pyLen) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByteArray(pyBuf);
_data.writeInt(pyLen);
mRemote.transact(Stub.TRANSACTION_imSearch, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int imDelSearch(int pos, boolean is_pos_in_splid, boolean clear_fixed_this_step) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(pos);
_data.writeInt(((is_pos_in_splid)?(1):(0)));
_data.writeInt(((clear_fixed_this_step)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_imDelSearch, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void imResetSearch() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_imResetSearch, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int imAddLetter(byte ch) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(ch);
mRemote.transact(Stub.TRANSACTION_imAddLetter, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String imGetPyStr(boolean decoded) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((decoded)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_imGetPyStr, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int imGetPyStrLen(boolean decoded) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((decoded)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_imGetPyStrLen, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int[] imGetSplStart() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_imGetSplStart, _data, _reply, 0);
_reply.readException();
_result = _reply.createIntArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String imGetChoice(int choiceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(choiceId);
mRemote.transact(Stub.TRANSACTION_imGetChoice, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String imGetChoices(int choicesNum) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(choicesNum);
mRemote.transact(Stub.TRANSACTION_imGetChoices, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.util.List<java.lang.String> imGetChoiceList(int choicesStart, int choicesNum, int sentFixedLen) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<java.lang.String> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(choicesStart);
_data.writeInt(choicesNum);
_data.writeInt(sentFixedLen);
mRemote.transact(Stub.TRANSACTION_imGetChoiceList, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArrayList();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int imChoose(int choiceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(choiceId);
mRemote.transact(Stub.TRANSACTION_imChoose, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int imCancelLastChoice() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_imCancelLastChoice, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int imGetFixedLen() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_imGetFixedLen, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean imCancelInput() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_imCancelInput, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void imFlushCache() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_imFlushCache, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int imGetPredictsNum(java.lang.String fixedStr) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(fixedStr);
mRemote.transact(Stub.TRANSACTION_imGetPredictsNum, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.util.List<java.lang.String> imGetPredictList(int predictsStart, int predictsNum) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<java.lang.String> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(predictsStart);
_data.writeInt(predictsNum);
mRemote.transact(Stub.TRANSACTION_imGetPredictList, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArrayList();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String imGetPredictItem(int predictNo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(predictNo);
mRemote.transact(Stub.TRANSACTION_imGetPredictItem, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String syncUserDict(java.lang.String tomerge) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(tomerge);
mRemote.transact(Stub.TRANSACTION_syncUserDict, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean syncBegin() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_syncBegin, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void syncFinish() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_syncFinish, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int syncPutLemmas(java.lang.String tomerge) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(tomerge);
mRemote.transact(Stub.TRANSACTION_syncPutLemmas, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String syncGetLemmas() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_syncGetLemmas, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int syncGetLastCount() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_syncGetLastCount, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int syncGetTotalCount() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_syncGetTotalCount, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void syncClearLastGot() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_syncClearLastGot, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int imSyncGetCapacity() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_imSyncGetCapacity, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getInt = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_setMaxLens = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_imSearch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_imDelSearch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_imResetSearch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_imAddLetter = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_imGetPyStr = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_imGetPyStrLen = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_imGetSplStart = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_imGetChoice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_imGetChoices = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_imGetChoiceList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_imChoose = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_imCancelLastChoice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_imGetFixedLen = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_imCancelInput = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_imFlushCache = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_imGetPredictsNum = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_imGetPredictList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_imGetPredictItem = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_syncUserDict = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_syncBegin = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_syncFinish = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_syncPutLemmas = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_syncGetLemmas = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
static final int TRANSACTION_syncGetLastCount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
static final int TRANSACTION_syncGetTotalCount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
static final int TRANSACTION_syncClearLastGot = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
static final int TRANSACTION_imSyncGetCapacity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
}
public int getInt() throws android.os.RemoteException;
public void setMaxLens(int maxSpsLen, int maxHzsLen) throws android.os.RemoteException;
public int imSearch(byte[] pyBuf, int pyLen) throws android.os.RemoteException;
public int imDelSearch(int pos, boolean is_pos_in_splid, boolean clear_fixed_this_step) throws android.os.RemoteException;
public void imResetSearch() throws android.os.RemoteException;
public int imAddLetter(byte ch) throws android.os.RemoteException;
public java.lang.String imGetPyStr(boolean decoded) throws android.os.RemoteException;
public int imGetPyStrLen(boolean decoded) throws android.os.RemoteException;
public int[] imGetSplStart() throws android.os.RemoteException;
public java.lang.String imGetChoice(int choiceId) throws android.os.RemoteException;
public java.lang.String imGetChoices(int choicesNum) throws android.os.RemoteException;
public java.util.List<java.lang.String> imGetChoiceList(int choicesStart, int choicesNum, int sentFixedLen) throws android.os.RemoteException;
public int imChoose(int choiceId) throws android.os.RemoteException;
public int imCancelLastChoice() throws android.os.RemoteException;
public int imGetFixedLen() throws android.os.RemoteException;
public boolean imCancelInput() throws android.os.RemoteException;
public void imFlushCache() throws android.os.RemoteException;
public int imGetPredictsNum(java.lang.String fixedStr) throws android.os.RemoteException;
public java.util.List<java.lang.String> imGetPredictList(int predictsStart, int predictsNum) throws android.os.RemoteException;
public java.lang.String imGetPredictItem(int predictNo) throws android.os.RemoteException;
public java.lang.String syncUserDict(java.lang.String tomerge) throws android.os.RemoteException;
public boolean syncBegin() throws android.os.RemoteException;
public void syncFinish() throws android.os.RemoteException;
public int syncPutLemmas(java.lang.String tomerge) throws android.os.RemoteException;
public java.lang.String syncGetLemmas() throws android.os.RemoteException;
public int syncGetLastCount() throws android.os.RemoteException;
public int syncGetTotalCount() throws android.os.RemoteException;
public void syncClearLastGot() throws android.os.RemoteException;
public int imSyncGetCapacity() throws android.os.RemoteException;
}
