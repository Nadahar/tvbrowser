/***************************************************************************
 *   Copyright (C) 2005 by Stefan Walkner                                  *
 *   walkner.stefan@sbg.at                                                 *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>

#include "logger.h"

/**
 * on type ERROR the method prints the message to stderr and exits
 * on other types the method prints the message to stdout and returns
 * USE the functions under this to log messages
 * @param logType the type of the log message
 * @param const char *msg message you would like to print
 * @param va_list *ap pointer to the created va_list
 */
void logMsg(const logType type, const char *msg, va_list *ap)
{
    char currentLogType[10];
    
    if ( type == LOG_ERROR )
    {
        fprintf(stderr, "ERROR> ");
        vfprintf(stderr, msg, *ap);
        fprintf(stderr, "\n");
        exit(1);
    }
    else
    {
        if ( type == LOG_DEBUG )
        {
            strncpy(currentLogType, "DEBUG", 10);
        }
        else if ( type == LOG_WARNING )
        {
            strncpy(currentLogType, "WARNING", 10);
        }
        else
        {
            strncpy(currentLogType, "UNKNOWN", 10);
        }
        
        fprintf(stdout, "%s> ", currentLogType);
        vfprintf(stdout, msg, *ap);
        fprintf(stdout, "\n");
    }
}

void logDebug(const char *msg, ...)
{
    va_list ap;
    va_start(ap, msg);
    va_end(ap);

    logMsg(LOG_DEBUG, msg, &ap);
}

void logWarning(const char *msg, ...)
{
    va_list ap;
    va_start(ap, msg);
    va_end(ap);

    logMsg(LOG_WARNING, msg, &ap);
}

void logError(const char *msg, ...)
{
    va_list ap;
    va_start(ap, msg);
    va_end(ap);

    logMsg(LOG_ERROR, msg, &ap);
}
