
#ifndef		_CH341_PORT_H
#define		_CH341_PORT_H

#ifdef __cplusplus
extern "C" {
#endif


#define		MAX_DEVICE_PATH_SIZE	128
#define		MAX_DEVICE_ID_SIZE		64


ULONG	WINAPI	CH341PtGetVersion( );


BOOL	WINAPI	CH341PtHandleIsCH341(
	HANDLE		iPortHandle );


BOOL	WINAPI	CH341PtNameIsCH341(
	PUCHAR		iPortName );


typedef		VOID	( CALLBACK	* mPCH341PT_NOTIFY_ROUTINE ) (
	LONG			iDevIndexAndEvent );



BOOL	WINAPI	CH341PtSetDevNotify(
	PCHAR						iDeviceID,
	mPCH341PT_NOTIFY_ROUTINE	iNotifyRoutine );



#ifdef __cplusplus
}
#endif

#endif