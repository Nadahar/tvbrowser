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

#ifndef _LOGGER_H
#define _LOGGER_H
    
#include <stdarg.h>

    typedef enum
{
    LOG_DEBUG,
    LOG_WARNING,
    LOG_ERROR
} logType;

    void logDebug(const char *msg, ...);
    void logWarning(const char *msg, ...);
    void logError(const char *msg, ...);
    
#endif
