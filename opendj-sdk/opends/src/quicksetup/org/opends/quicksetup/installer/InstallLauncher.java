/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Portions Copyright 2006-2007 Sun Microsystems, Inc.
 */
package org.opends.quicksetup.installer;

import org.opends.messages.Message;
import static org.opends.messages.QuickSetupMessages.*;
import static org.opends.messages.ToolMessages.*;
import static org.opends.server.tools.ToolConstants.*;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.opends.quicksetup.Launcher;
import org.opends.quicksetup.CliApplication;
import org.opends.quicksetup.Installation;
import org.opends.quicksetup.QuickSetupLog;
import org.opends.quicksetup.util.Utils;
import org.opends.server.util.ServerConstants;
import org.opends.server.util.SetupUtils;
import org.opends.server.util.args.ArgumentParser;
import org.opends.server.util.args.BooleanArgument;
import org.opends.server.util.args.FileBasedArgument;
import org.opends.server.util.args.IntegerArgument;
import org.opends.server.util.args.StringArgument;

/**
 * This class is called by the setup command lines to launch the installation of
 * the Directory Server. It just checks the command line arguments and the
 * environment and determines whether the graphical or the command line
 * based setup much be launched.
 */
public class InstallLauncher extends Launcher {

  /** Prefix for log files. */
  static public final String LOG_FILE_PREFIX = "opends-setup-";

  /** Suffix for log files. */
  static public final String LOG_FILE_SUFFIX = ".log";

  static private final Logger LOG =
          Logger.getLogger(InstallLauncher.class.getName());

  /**
   * The main method which is called by the setup command lines.
   *
   * @param args the arguments passed by the command lines.  In the case
   * we want to launch the cli setup they are basically the arguments that we
   * will pass to the org.opends.server.tools.InstallDS class.
   */
  public static void main(String[] args) {
    try {
      QuickSetupLog.initLogFileHandler(
              File.createTempFile(LOG_FILE_PREFIX, LOG_FILE_SUFFIX));
    } catch (Throwable t) {
      System.err.println("Unable to initialize log");
      t.printStackTrace();
    }
    new InstallLauncher(args).launch();
  }

  private ArgumentParser argParser;

  /**
   * Creates a launcher.
   *
   * @param args the arguments passed by the command lines.
   */
  public InstallLauncher(String[] args) {
    super(args);

    String scriptName;
    if (Utils.isWindows()) {
      scriptName = Installation.WINDOWS_SETUP_FILE_NAME;
    } else {
      scriptName = Installation.UNIX_SETUP_FILE_NAME;
    }
    System.setProperty(ServerConstants.PROPERTY_SCRIPT_NAME, scriptName);

    argParser = new ArgumentParser(getClass().getName(),
        INFO_SETUP_LAUNCHER_USAGE_DESCRIPTION.get(),
        false);
    BooleanArgument   addBaseEntry;
    BooleanArgument   cliMode;
    BooleanArgument   showUsage;
    BooleanArgument   silentInstall;
    BooleanArgument   skipPortCheck;
    BooleanArgument   enableWindowsService;
    FileBasedArgument rootPWFile;
    IntegerArgument   ldapPort;
    IntegerArgument   jmxPort;
    IntegerArgument   sampleData;
    StringArgument    baseDN;
    StringArgument    importLDIF;
    StringArgument    rootDN;
    StringArgument    rootPWString;

    try
    {
      cliMode = new BooleanArgument("cli", 'c', OPTION_LONG_CLI,
          INFO_INSTALLDS_DESCRIPTION_CLI.get());
      argParser.addArgument(cliMode);

      silentInstall = new BooleanArgument("silent", 's', "silentInstall",
          INFO_INSTALLDS_DESCRIPTION_SILENT.get());
      argParser.addArgument(silentInstall);

      baseDN = new StringArgument("basedn", OPTION_SHORT_BASEDN,
          OPTION_LONG_BASEDN, false, true, true,
          OPTION_VALUE_BASEDN,
          "dc=example,dc=com", null,
          INFO_INSTALLDS_DESCRIPTION_BASEDN.get());
      argParser.addArgument(baseDN);

      addBaseEntry = new BooleanArgument("addbase", 'a', "addBaseEntry",
          INFO_INSTALLDS_DESCRIPTION_ADDBASE.get());
      argParser.addArgument(addBaseEntry);

      importLDIF = new StringArgument("importldif", OPTION_SHORT_LDIF_FILE,
          OPTION_LONG_LDIF_FILE, false,
          true, true, OPTION_VALUE_LDIF_FILE,
          null, null,
          INFO_INSTALLDS_DESCRIPTION_IMPORTLDIF.get());
      argParser.addArgument(importLDIF);

      sampleData = new IntegerArgument("sampledata", 'd', "sampleData", false,
          false, true, "{numEntries}", 0, null,
          true, 0, false, 0,
          INFO_INSTALLDS_DESCRIPTION_SAMPLE_DATA.get());
      argParser.addArgument(sampleData);

      ldapPort = new IntegerArgument("ldapport", OPTION_SHORT_PORT,
          "ldapPort", false, false,
          true, OPTION_VALUE_PORT, 389,
          null, true, 1, true, 65535,
          INFO_INSTALLDS_DESCRIPTION_LDAPPORT.get());
      argParser.addArgument(ldapPort);

      jmxPort = new IntegerArgument("jmxport", 'x', "jmxPort", false, false,
          true, "{jmxPort}",
          SetupUtils.getDefaultJMXPort(), null, true,
          1, true, 65535,
          INFO_INSTALLDS_DESCRIPTION_JMXPORT.get());
      argParser.addArgument(jmxPort);

      skipPortCheck = new BooleanArgument("skipportcheck", 'S', "skipPortCheck",
          INFO_INSTALLDS_DESCRIPTION_SKIPPORT.get());
      argParser.addArgument(skipPortCheck);

      rootDN = new StringArgument("rootdn",OPTION_SHORT_ROOT_USER_DN,
          OPTION_LONG_ROOT_USER_DN, false, true,
          true, OPTION_VALUE_ROOT_USER_DN,
          "cn=Directory Manager",
          null, INFO_INSTALLDS_DESCRIPTION_ROOTDN.get());
      argParser.addArgument(rootDN);

      rootPWString = new StringArgument("rootpwstring", OPTION_SHORT_BINDPWD,
          "rootUserPassword",
          false, false, true,
          "{password}", null,
          null,
          INFO_INSTALLDS_DESCRIPTION_ROOTPW.get());
      argParser.addArgument(rootPWString);

      rootPWFile = new FileBasedArgument("rootpwfile",
          OPTION_SHORT_BINDPWD_FILE,
          "rootUserPasswordFile", false, false,
          OPTION_VALUE_BINDPWD_FILE,
          null, null, INFO_INSTALLDS_DESCRIPTION_ROOTPWFILE.get());
      argParser.addArgument(rootPWFile);

      enableWindowsService = new BooleanArgument("enablewindowsservice", 'e',
          "enableWindowsService",
          INFO_INSTALLDS_DESCRIPTION_ENABLE_WINDOWS_SERVICE.get());
      if (SetupUtils.isWindows())
      {
        argParser.addArgument(enableWindowsService);
      }

      showUsage = new BooleanArgument("help", OPTION_SHORT_HELP,
          OPTION_LONG_HELP,
          INFO_INSTALLDS_DESCRIPTION_HELP.get());
      argParser.addArgument(showUsage);
      argParser.setUsageArgument(showUsage);
    }
    catch (Throwable t)
    {
      System.out.println("ERROR: "+t);
      t.printStackTrace();
    }
  }

  /**
   * {@inheritDoc}
   */
  protected void guiLaunchFailed(String logFileName) {
    if (logFileName != null)
    {
      System.err.println(INFO_SETUP_LAUNCHER_GUI_LAUNCHED_FAILED_DETAILS.get(
              logFileName));
    }
    else
    {
      System.err.println(INFO_SETUP_LAUNCHER_GUI_LAUNCHED_FAILED.get());
    }
  }

  /**
   * {@inheritDoc}
   */
  public ArgumentParser getArgumentParser() {
    return this.argParser;
  }

  /**
   * {@inheritDoc}
   */
  protected void willLaunchGui() {
    System.out.println(INFO_SETUP_LAUNCHER_LAUNCHING_GUI.get());
    System.setProperty("org.opends.quicksetup.Application.class",
            "org.opends.quicksetup.installer.offline.OfflineInstaller");
  }

  /**
   * {@inheritDoc}
   */
  protected Message getFrameTitle() {
    return INFO_FRAME_INSTALL_TITLE.get();
  }

  /**
   * {@inheritDoc}
   */
  protected CliApplication createCliApplication() {
    // Setup currently has no implemented CliApplication
    // but rather relies on InstallDS from the server
    // package.  Note that launchCli is overloaded
    // to run this application instead of the default
    // behavior in Launcher
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int launchCli(CliApplication cliApp) {
    System.setProperty("org.opends.quicksetup.cli", "true");

    if (Utils.isWindows()) {
      System.setProperty("org.opends.server.scriptName",
              Installation.WINDOWS_SETUP_FILE_NAME);
    } else {
      System.setProperty("org.opends.server.scriptName",
              Installation.UNIX_SETUP_FILE_NAME);
    }
    ArrayList<String> newArgList = new ArrayList<String>();
    if (args != null) {
      for (String arg : args) {
        if (!arg.equalsIgnoreCase("--cli") &&
                !arg.equalsIgnoreCase("-c")) {
          newArgList.add(arg);
        }
      }
    }
    newArgList.add("--configClass");
    newArgList.add("org.opends.server.extensions.ConfigFileHandler");
    newArgList.add("--configFile");
    Installation installation = Installation.getLocal();
    newArgList.add(Utils.getPath(installation.getCurrentConfigurationFile()));

    String[] newArgs = new String[newArgList.size()];
    newArgList.toArray(newArgs);
    LOG.log(Level.INFO, "Launching 'installMain' with args " +
            Utils.listToString(newArgList, " "));
    return org.opends.server.tools.InstallDS.installMain(newArgs);
  }
}
