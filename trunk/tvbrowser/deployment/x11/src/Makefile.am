
# set the include path found by configure
INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux $(all_includes)

# the library search path.
libDesktopIndicator_so_LDFLAGS = -shared -Wl
noinst_PROGRAMS = libDesktopIndicator.so
libDesktopIndicator_so_SOURCES = jni_wrapper.c x11_systray_window.c

SUBDIRS = eclipse_java logger
noinst_HEADERS = x11_systray_window.h
libDesktopIndicator_so_LDADD = $(top_builddir)/src/logger/liblogger.a \
	-L/usr/X11R6/lib -lX11 -lm -lc -L$(JAVA_HOME)/jre/lib/i386/ -ljawt
