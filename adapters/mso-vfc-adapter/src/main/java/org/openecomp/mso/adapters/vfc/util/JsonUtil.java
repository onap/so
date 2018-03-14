/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.adapters.vfc.util;

import java.io.IOException;

import org.openecomp.mso.adapters.vfc.constant.HttpCode;
import org.openecomp.mso.adapters.vfc.exceptions.ApplicationException;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interface for json analyzing.<br/>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-9-6
 */
public class JsonUtil {

  /**
   * Log service
   */
  private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);

  /**
   * Mapper.
   */
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.setConfig(MAPPER.getDeserializationConfig().without(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    MAPPER.setSerializationInclusion(Include.NON_NULL);
  }

  /**
   * Constructor<br/>
   * <p>
   * </p>
   * 
   * @since ONAP Amsterdam Release 2017-9-6
   */
  private JsonUtil() {

  }

  /**
   * Parse the string in form of json.<br/>
   * 
   * @param jsonstr json string.
   * @param type that convert json string to
   * @return model object
   * @since ONAP Amsterdam Release 2017-9-6
   */
  public static <T> T unMarshal(String jsonstr, Class<T> type) throws ApplicationException {
    try {
      return MAPPER.readValue(jsonstr, type);
    } catch (IOException e) {
      LOGGER.error(MessageEnum.RA_NS_EXC, "", "", MsoLogger.ErrorCode.BusinessProcesssError,
          "fail to unMarshal json", e);
      throw new ApplicationException(HttpCode.BAD_REQUEST, "fail to unMarshal json");
    }
  }

  /**
   * Parse the string in form of json.<br/>
   * 
   * @param jsonstr json string.
   * @param type that convert json string to
   * @return model object
   * @since ONAP Amsterdam Release 2017-9-6
   */
  public static <T> T unMarshal(String jsonstr, TypeReference<T> type) throws ApplicationException {
    try {
      return MAPPER.readValue(jsonstr, type);
    } catch (IOException e) {
      LOGGER.error(MessageEnum.RA_NS_EXC, "", "", MsoLogger.ErrorCode.BusinessProcesssError,
          "fail to unMarshal json", e);
      throw new ApplicationException(HttpCode.BAD_REQUEST, "fail to unMarshal json");
    }
  }

  /**
   * Convert object to json string.<br/>
   * 
   * @param srcObj data object
   * @return json string
   * @since ONAP Amsterdam Release 2017-9-6
   */
  public static String marshal(Object srcObj) throws ApplicationException {
    try {
      return MAPPER.writeValueAsString(srcObj);
    } catch (IOException e) {
      LOGGER.error(MessageEnum.RA_NS_EXC, "", "", MsoLogger.ErrorCode.BusinessProcesssError,
          "fail to marshal json", e);
      throw new ApplicationException(HttpCode.BAD_REQUEST, "srcObj marshal failed!");
    }
  }

  /**
   * Get mapper.<br/>
   * 
   * @return mapper
   * @since ONAP Amsterdam Release 2017-9-6
   */
  public static ObjectMapper getMapper() {
    return MAPPER;
  }
}
