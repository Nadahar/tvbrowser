
#ifndef __DesktopIndicatorImages_h__
#define __DesktopIndicatorImages_h__


class DesktopIndicatorImages
{
public:

    jint add( jint resourceid );    //  loads icon from this DLL (DVB19Oct99)
	jint add( const char *filename );
	void remove( jint handle );
};

extern DesktopIndicatorImages g_DesktopIndicatorImages;


#endif
