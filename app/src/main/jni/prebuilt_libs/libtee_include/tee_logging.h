/*****************************************************************************
** Copyright (C) 2014 Intel Corporation.                                    **
**                                                                          **
** Licensed under the Apache License, Version 2.0 (the "License");          **
** you may not use this file except in compliance with the License.         **
** You may obtain a copy of the License at                                  **
**                                                                          **
**      http://www.apache.org/licenses/LICENSE-2.0                          **
**                                                                          **
** Unless required by applicable law or agreed to in writing, software      **
** distributed under the License is distributed on an "AS IS" BASIS,        **
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. **
** See the License for the specific language governing permissions and      **
** limitations under the License.                                           **
*****************************************************************************/

#ifndef __TEE_LOGGING_H__
#define __TEE_LOGGING_H__

#if (defined TA_PLUGIN || defined OT_LOGGING)
#include <syslog.h>

#define DBG_LOCATION "%s:%s:%d  "

/*!
  Print message to syslog with additional information.
*/
#define OT_LOG(level, message, ...)                                                                \
	syslog(level, DBG_LOCATION message, __FILE__, __func__, __LINE__, ##__VA_ARGS__)

/*!
  Print message to syslog without additional information.
*/
#define OT_LOG1(level, message, ...) syslog(level, message, ##__VA_ARGS__)

/*!
  Print LOG_ERR level message to syslog
*/
#define OT_LOG_ERR(message, ...) syslog(LOG_ERR, message, ##__VA_ARGS__)

/*!
  Print LOG_ERR level integer to syslog
*/
#define OT_LOG_INT(integer) syslog(LOG_ERR, "%d", integer)

/*!
  Print LOG_ERR level string to syslog
*/
#define OT_LOG_STR(str) syslog(LOG_ERR, "%s", str)

#else

#define OT_LOG(...) do {} while (0)
#define OT_LOG1(...) do {} while (0)
#define OT_LOG_ERR(...) do {} while (0)
#define OT_LOG_INT(...) do {} while (0)
#define OT_LOG_STR(...) do {} while (0)

#endif

#endif /* __TEE_LOGGING_H__ */
