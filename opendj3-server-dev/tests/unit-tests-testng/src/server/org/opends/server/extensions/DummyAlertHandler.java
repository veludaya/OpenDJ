/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2008 Sun Microsystems, Inc.
 *      Portions Copyright 2014 ForgeRock AS
 */
package org.opends.server.extensions;



import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.admin.std.server.AlertHandlerCfg;
import org.opends.server.api.AlertGenerator;
import org.opends.server.api.AlertHandler;
import org.opends.server.config.ConfigException;
import org.opends.server.types.ConfigChangeResult;
import org.opends.server.types.InitializationException;
import org.forgerock.opendj.ldap.ResultCode;
import org.forgerock.i18n.LocalizableMessage;


/**
 * This class implements a Directory Server alert handler that only provides a
 * mechanism to determine the number of times it is invoked.
 */
public class DummyAlertHandler
       implements AlertHandler<AlertHandlerCfg>,
                  ConfigurationChangeListener<AlertHandlerCfg>
{
  // The current configuration for this alert handler.
  private AlertHandlerCfg currentConfig;

  // The number of times this alert handler has been invoked.
  private static AtomicInteger alertCount = new AtomicInteger(0);


  /**
   * Creates a new instance of this SMTP alert handler.
   */
  public DummyAlertHandler()
  {
    super();

    // All initialization should be done in the initializeAlertHandler method.
  }



  /**
   * {@inheritDoc}
   */
  public void initializeAlertHandler(AlertHandlerCfg configuration)
       throws ConfigException, InitializationException
  {
    configuration.addChangeListener(this);
    currentConfig = configuration;
  }



  /**
   * {@inheritDoc}
   */
  public AlertHandlerCfg getAlertHandlerConfiguration()
  {
    return currentConfig;
  }



  /**
   * {@inheritDoc}
   */
  public boolean isConfigurationAcceptable(AlertHandlerCfg configuration,
                                           List<LocalizableMessage> unacceptableReasons)
  {
    return true;
  }



  /**
   * {@inheritDoc}
   */
  public void finalizeAlertHandler()
  {
    // No action is required.
  }



  /**
   * {@inheritDoc}
   */
  public void sendAlertNotification(AlertGenerator generator, String alertType,
                                    LocalizableMessage alertMessage)
  {
    alertCount.incrementAndGet();
  }



  /**
   * Retrieves the number of times that this alert handler has been invoked.
   *
   * @return  The number of times that this alert handler has been invoked.
   */
  public static int getAlertCount()
  {
    return alertCount.get();
  }



  /**
   * {@inheritDoc}
   */
  public boolean isConfigurationChangeAcceptable(AlertHandlerCfg configuration,
                      List<LocalizableMessage> unacceptableReasons)
  {
    return true;
  }



  /**
   * {@inheritDoc}
   */
  public ConfigChangeResult applyConfigurationChange(
                                 AlertHandlerCfg configuration)
  {
    currentConfig = configuration;

    return new ConfigChangeResult(ResultCode.SUCCESS, false);
  }
}
