/*
 * Copyright (C) Cristian Sulea ( http://cristian.sulea.net )
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jatoo.cli.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import jatoo.cli.AbstractCLICommand;
import jatoo.image.ImageFileFilter;
import jatoo.image.ImageMetadata;
import jatoo.image.ImageMetadataHandler;
import jatoo.image.ImageUtils;

/**
 * The "image" command for the JaToo CLI project.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 2.0, August 2, 2017
 */
public class JatooCLICommand extends AbstractCLICommand {

  private static final String OPTION_METADATA = "metadata";

  @Override
  public void execute(final String[] args) {

    //
    // options

    OptionGroup optionGroup = new OptionGroup();
    optionGroup.setRequired(true);
    optionGroup.addOption(Option.builder("resize").desc(getText("desc.option.resize")).build());
    // optionGroup.addOption(Option.builder("crop").desc(getText("desc.option.crop")).build());
    // optionGroup.addOption(Option.builder("rotate").desc(getText("desc.option.rotate")).build());
    optionGroup.addOption(Option.builder("rename").desc(getText("desc.option.rename")).build());
    optionGroup.addOption(Option.builder(OPTION_METADATA).desc(getText("desc.option." + OPTION_METADATA)).build());

    Options options = new Options();
    options.addOptionGroup(optionGroup);

    //
    // parse

    try {

      CommandLine line = parse(options, args, true);

      //
      // and work

      if (line.hasOption("resize")) {
        resize(line.getArgs());
      }

      // else if (line.hasOption("crop")) {
      // // resize(line.getArgs());
      // }
      //
      // else if (line.hasOption("rotate")) {
      // // resize(line.getArgs());
      // }

      else if (line.hasOption("rename")) {
        rename(line.getArgs());
      }

      else if (line.hasOption(OPTION_METADATA)) {
        metadata(line.getArgs());
      }

      else {
        throwUnknownOption();
      }
    }

    catch (Throwable t) {
      printHelp("-image", options, t);
    }
  }

  private void resize(final String[] args) {

    //
    // options

    OptionGroup optionGroup = new OptionGroup();
    optionGroup.setRequired(true);
    optionGroup.addOption(Option.builder("fit").desc(getText("desc.option.resize.fit")).build());
    optionGroup.addOption(Option.builder("fill").desc(getText("desc.option.resize.fill")).build());

    Options options = new Options();
    options.addOptionGroup(optionGroup);

    //
    // parse

    try {

      CommandLine line = parse(options, args, true);

      //
      // and work

      if (line.hasOption("fit")) {
        resize(line.getArgs(), true);
      }

      else if (line.hasOption("fill")) {
        resize(line.getArgs(), false);
      }

      else {
        throwUnknownOption();
      }
    }

    catch (Throwable e) {
      printHelp("-image -resize", options, e);
    }
  }

  private void resize(final String[] args, final boolean fit) {

    //
    // options

    Options options = new Options();
    options.addOption(Option.builder("width").hasArg().required(true).desc(getText("desc.option.resize." + (fit ? "fit" : "fill") + ".width")).build());
    options.addOption(Option.builder("height").hasArg().required(true).desc(getText("desc.option.resize." + (fit ? "fit" : "fill") + ".height")).build());
    options.addOption(Option.builder("removeMetadata").required(false).desc(getText("desc.option.resize.removeMetadata")).build());
    options.addOption(Option.builder("overwrite").required(false).desc(getText("desc.option.resize.overwrite")).build());
    options.addOption(Option.builder("src").hasArg().required(true).desc(getText("desc.option.resize.src")).build());
    options.addOption(Option.builder("dst").hasArg().required(true).desc(getText("desc.option.resize.dst")).build());

    //
    // parse

    try {

      CommandLine line = parse(options, args, true);

      //
      // and work

      int width = Integer.parseInt(line.getOptionValue("width"));
      int height = Integer.parseInt(line.getOptionValue("height"));

      boolean removeMetadata = line.hasOption("removeMetadata");
      boolean overwrite = line.hasOption("overwrite");

      File src = new File(line.getOptionValue("src"));
      File dst = new File(line.getOptionValue("dst"));

      if (!src.exists()) {
        throw new FileNotFoundException("source file (or folder) does not exists: " + src.getAbsolutePath());
      }

      if (!dst.exists()) {
        if (!dst.mkdirs()) {
          throw new IllegalArgumentException("dst.mkdirs() failed");
        }
      }
      if (!dst.isDirectory()) {
        throw new NotDirectoryException(dst.getAbsolutePath());
      }

      if (src.isFile()) {

        File srcImageFile = src;
        File dstImageFile = new File(dst, srcImageFile.getName());

        if (!overwrite) {
          if (dstImageFile.exists()) {
            throw new FileAlreadyExistsException(dstImageFile.getPath(), null, "file already exists" + System.getProperty("line.separator") + "use \"-overwrite\" option to overwrite existing file)");
          }
        }

        System.out.println(getText("text.resizing.1.image", srcImageFile.getName()));

        ImageUtils.resizeTo(fit, srcImageFile, dstImageFile, width, height);
        if (!removeMetadata) {
          if (!ImageMetadataHandler.getInstance().copyMetadata(srcImageFile, dstImageFile)) {
            throw new IOException("failed to copy the metadata");
          }
        }

        System.out.println(getText("text.done"));
      }

      else if (src.isDirectory()) {

        File[] srcImageFiles = src.listFiles(ImageFileFilter.getInstance());

        if (srcImageFiles == null) {
          throw new IllegalArgumentException("src.listFiles() returned \"null\"");
        }

        if (!overwrite) {
          for (File srcImageFile : srcImageFiles) {
            File dstImageFile = new File(dst, srcImageFile.getName());
            if (dstImageFile.exists()) {
              throw new FileAlreadyExistsException(dstImageFile.getPath(), null, "file already exists" + System.getProperty("line.separator") + "use \"-overwrite\" option to overwrite existing files");
            }
          }
        }

        System.out.println(getText("text.resizing.n.images.1", srcImageFiles.length, src.getPath()));

        for (File srcImageFile : srcImageFiles) {
          File dstImageFile = new File(dst, srcImageFile.getName());

          ImageUtils.resizeTo(fit, srcImageFile, dstImageFile, width, height);
          if (!removeMetadata) {
            if (!ImageMetadataHandler.getInstance().copyMetadata(srcImageFile, dstImageFile)) {
              throw new IOException("failed to copy the metadata");
            }
          }

          System.out.println(getText("text.resizing.n.images.2", dstImageFile.getName()));
        }

        System.out.println(getText("text.done"));
      }

      else {
        throw new IllegalArgumentException("illegal input");
      }
    }

    catch (Throwable e) {
      printHelp("-image -resize -" + (fit ? "fit" : "fill"), options, e);
    }
  }

  private void rename(final String[] args) {

    //
    // options

    OptionGroup caseGroup = new OptionGroup();
    caseGroup.setRequired(false);
    caseGroup.addOption(Option.builder("toLowerCase").desc(getText("desc.option.resize.toLowerCase")).build());
    caseGroup.addOption(Option.builder("toUpperCase").desc(getText("desc.option.resize.toUpperCase")).build());

    Options options = new Options();
    options.addOption(Option.builder("pattern").hasArg().required(true).desc(getText("desc.option.rename.pattern")).build());
    options.addOption(Option.builder("counterDigits").hasArg().required(false).desc(getText("desc.option.resize.counterDigits")).build());
    options.addOptionGroup(caseGroup);
    options.addOption(Option.builder("src").hasArg().required(true).desc(getText("desc.option.resize.src")).build());
    options.addOption(Option.builder("dst").hasArg().required(true).desc(getText("desc.option.resize.dst")).build());

    //
    // parse

    try {

      CommandLine line = parse(options, args, true);

      //
      // and work

      String pattern = line.getOptionValue("pattern");
      int counterDigits = Integer.parseInt(line.getOptionValue("counterDigits", "0"));
      boolean toLowerCase = line.hasOption("toLowerCase");
      boolean toUpperCase = line.hasOption("toUpperCase");

      File src = new File(line.getOptionValue("src"));
      File dst = new File(line.getOptionValue("dst"));

      NumberFormat counterNF = NumberFormat.getIntegerInstance();
      counterNF.setGroupingUsed(false);
      counterNF.setMinimumIntegerDigits(counterDigits);

      if (!src.exists()) {
        throw new FileNotFoundException("source file (or folder) does not exists: " + src.getAbsolutePath());
      }

      if (!dst.exists()) {
        if (!dst.mkdirs()) {
          throw new IllegalArgumentException("dst.mkdirs() failed");
        }
      }
      if (!dst.isDirectory()) {
        throw new NotDirectoryException(dst.getAbsolutePath());
      }

      if (src.isFile()) {

        final File srcImageFile = src;

        System.out.println(getText("text.renaming.image.1", srcImageFile.getPath()));

        final Date date = ImageMetadataHandler.getInstance().getDateTimeOriginal(srcImageFile);

        if (date == null) {
          throw new IllegalArgumentException("the image does not have DateTimeOriginal metadata");
        }

        String dstImageFileName = new SimpleDateFormat(pattern).format(date) + renameGetFileExtension(srcImageFile, true);

        if (toLowerCase) {
          dstImageFileName = dstImageFileName.toLowerCase();
        } else if (toUpperCase) {
          dstImageFileName = dstImageFileName.toUpperCase();
        }

        File dstImageFile = new File(dst, dstImageFileName);

        System.out.println(getText("text.renaming.image.2", dstImageFile.getPath()));

        Files.copy(srcImageFile.toPath(), dstImageFile.toPath());

        System.out.println(getText("text.done"));
      }

      else if (src.isDirectory()) {

        final File[] srcImageFiles = src.listFiles(ImageFileFilter.getInstance());

        if (srcImageFiles == null) {
          throw new IllegalArgumentException("src.listFiles() returned \"null\"");
        }

        System.out.println(getText("text.renaming.images.1", srcImageFiles.length));
        System.out.println(getText("text.renaming.images.2", src.getPath()));
        System.out.println(getText("text.renaming.images.3", dst.getPath()));

        for (int i = 0; i < srcImageFiles.length; i++) {
          final File srcImageFile = srcImageFiles[i];
          final Date date = ImageMetadataHandler.getInstance().getDateTimeOriginal(srcImageFile);

          String dstImageFileName;

          if (date == null) {
            dstImageFileName = counterNF.format(i + 1) + renameGetFileExtension(srcImageFile, true);
          }

          else {

            final String dstPattern = pattern.replaceAll("\\$\\{counter\\}", counterNF.format(i + 1));
            final SimpleDateFormat dstSDF = new SimpleDateFormat(dstPattern);

            dstImageFileName = dstSDF.format(date) + renameGetFileExtension(srcImageFile, true);
          }

          if (toLowerCase) {
            dstImageFileName = dstImageFileName.toLowerCase();
          } else if (toUpperCase) {
            dstImageFileName = dstImageFileName.toUpperCase();
          }

          Files.copy(srcImageFile.toPath(), new File(dst, dstImageFileName).toPath());

          System.out.println(getText("text.renaming.images.4", srcImageFile.getName(), dstImageFileName));
        }

        System.out.println(getText("text.done"));
      }

      else {
        throw new IllegalArgumentException("illegal input");
      }
    }

    catch (Throwable e) {
      printHelp("-image -rename", options, e);
    }
  }

  private String renameGetFileExtension(final File file, final boolean includeSeparator) {
    final String filename = file.getName();
    final int indexSeparator = filename.lastIndexOf('.');
    if (indexSeparator == -1) {
      return "";
    } else {
      if (includeSeparator) {
        return "." + filename.substring(indexSeparator + 1);
      } else {
        return filename.substring(indexSeparator + 1);
      }
    }
  }

  private void metadata(final String[] args) {

    //
    // options

    OptionGroup actionGroup = new OptionGroup();
    actionGroup.setRequired(true);
    actionGroup.addOption(Option.builder("get").desc(getText("desc.option." + OPTION_METADATA + ".get")).build());
    actionGroup.addOption(Option.builder("set").desc(getText("desc.option." + OPTION_METADATA + ".set")).build());

    Options options = new Options();
    options.addOption(Option.builder("src").hasArg().required(true).desc(getText("desc.option." + OPTION_METADATA + ".src")).build());
    options.addOptionGroup(actionGroup);

    //
    // parse

    try {

      CommandLine line = parse(options, args, true);

      //
      // and work

      File src = new File(line.getOptionValue("src"));

      if (!src.exists()) {
        throw new FileNotFoundException("source file (or folder) does not exists: " + src.getAbsolutePath());
      }

      if (line.hasOption("get")) {
        metadataGet(src, line.getArgs());
      }

      else if (line.hasOption("set")) {
        metadataSet(src, line.getArgs());
      }

      else {
        throwUnknownOption();
      }
    }

    catch (Throwable e) {
      printHelp("-image -" + OPTION_METADATA, options, e);
    }
  }

  private void metadataGet(final File src, final String[] args) {

    //
    // options

    OptionGroup optionGroup = new OptionGroup();
    optionGroup.setRequired(true);
    optionGroup.addOption(Option.builder("all").desc(getText("desc.option." + OPTION_METADATA + ".get.all")).build());
    optionGroup.addOption(Option.builder("DateTimeOriginal").desc(getText("desc.option." + OPTION_METADATA + ".get.DateTimeOriginal")).build());

    Options options = new Options();
    options.addOptionGroup(optionGroup);
    options.addOption(Option.builder("DateTimeOriginalPattern").required(false).hasArg().desc(getText("desc.option." + OPTION_METADATA + ".get.DateTimeOriginalPattern")).build());

    //
    // parse

    try {

      CommandLine line = parse(options, args, true);

      //
      // and work

      boolean getAll = line.hasOption("all");
      boolean getDateTimeOriginal = line.hasOption("DateTimeOriginal");
      String patternDateTimeOriginal = line.getOptionValue("DateTimeOriginalPattern");

      if (src.isFile()) {
        metadataGetPrint(src, getAll, getDateTimeOriginal, patternDateTimeOriginal);
      }

      else if (src.isDirectory()) {

        File[] srcImageFiles = src.listFiles(ImageFileFilter.getInstance());

        if (srcImageFiles == null) {
          throw new IllegalArgumentException("src.listFiles() returned \"null\"");
        }

        for (File srcImageFile : srcImageFiles) {
          metadataGetPrint(srcImageFile, getAll, getDateTimeOriginal, patternDateTimeOriginal);
        }
      }

      else {
        throw new IllegalArgumentException("illegal input");
      }
    }

    catch (Throwable e) {
      printHelp("-image -" + OPTION_METADATA + " -get", options, e);
    }
  }

  private void metadataGetPrint(final File file, final boolean getAll, final boolean getDateTimeOriginal, final String patternDateTimeOriginal) {

    System.out.println(file);

    SimpleDateFormat dateFormat = new SimpleDateFormat(patternDateTimeOriginal == null ? "yyyy-MM-dd HH:mm:ss" : patternDateTimeOriginal);

    if (getAll) {

      ImageMetadata metadata = ImageMetadataHandler.getInstance().getMetadata(file);

      System.out.println("   DateTimeOriginal -> " + dateFormat.format(metadata.getDateTimeOriginal()));
      System.out.println("   ImageWidth -> " + metadata.getImageWidth());
      System.out.println("   ImageHeight -> " + metadata.getImageHeight());
    }

    else {

      if (getDateTimeOriginal) {
        System.out.println("   DateTimeOriginal -> " + dateFormat.format(ImageMetadataHandler.getInstance().getDateTimeOriginal(file)));
      }

      else {
        throwUnknownOption();
      }
    }

    System.out.println();
  }

  private void metadataSet(final File src, final String[] args) {

    //
    // options

    Option.Builder builderDateTimeOriginal = Option.builder("DateTimeOriginal").desc(getText("desc.option." + OPTION_METADATA + ".set.DateTimeOriginal"));
    builderDateTimeOriginal.hasArgs().numberOfArgs(6).argName(getText("desc.option." + OPTION_METADATA + ".set.DateTimeOriginal.argName"));

    Option.Builder builderDateTimeOriginalFromFileName = Option.builder("DateTimeOriginalFromFileName").desc(getText("desc.option." + OPTION_METADATA + ".set.DateTimeOriginalFromFileName"));
    builderDateTimeOriginalFromFileName.argName(getText("desc.option." + OPTION_METADATA + ".set.DateTimeOriginalFromFileName.argName"));

    OptionGroup optionGroup = new OptionGroup();
    optionGroup.setRequired(true);
    optionGroup.addOption(builderDateTimeOriginal.build());
    optionGroup.addOption(builderDateTimeOriginalFromFileName.build());

    Options options = new Options();
    options.addOptionGroup(optionGroup);

    //
    // parse

    try {

      CommandLine line = parse(options, args, true);

      //
      // and work

      if (line.hasOption("DateTimeOriginal")) {

        String[] values = line.getOptionValues("DateTimeOriginal");

        int year = Integer.parseInt(values[0]);
        int month = Integer.parseInt(values[1]);
        int day = Integer.parseInt(values[2]);
        int hour = Integer.parseInt(values[3]);
        int minute = Integer.parseInt(values[4]);
        int second = Integer.parseInt(values[5]);

        ImageMetadataHandler.getInstance().setDateTimeOriginal(src, year, month, day, hour, minute, second);
      }

      else if (line.hasOption("DateTimeOriginalFromFileName")) {
        metadataSetDateTimeOriginalFromFileName(src, line.getArgs());
      }

      else {
        throwUnknownOption();
      }
    }

    catch (Throwable e) {
      printHelp("-image -" + OPTION_METADATA + " -set", options, e);
    }
  }

  private void metadataSetDateTimeOriginalFromFileName(final File src, final String[] args) {

    //
    // options

    Options options = new Options();
    options.addOption(Option.builder("pattern").required(true).hasArg().desc(getText("desc.option." + OPTION_METADATA + ".set.DateTimeOriginalFromFileName.pattern")).build());
    options.addOption(Option.builder("correction").required(false).hasArg().desc(getText("desc.option." + OPTION_METADATA + ".set.DateTimeOriginalFromFileName.correction")).build());

    //
    // parse

    try {

      CommandLine line = parse(options, args, true);

      //
      // and work

      String pattern = line.getOptionValue("pattern");
      String correction = line.getOptionValue("correction");

      if (src.isFile()) {

        Calendar c = Calendar.getInstance();
        c.setTime(new SimpleDateFormat(pattern).parse(src.getName()));

        if (correction != null) {
          c.add(Calendar.HOUR_OF_DAY, Integer.parseInt(correction));
        }

        ImageMetadataHandler.getInstance().setDateTimeOriginal(src, c.getTime());
      }

      else if (src.isDirectory()) {

        File[] srcImageFiles = src.listFiles(ImageFileFilter.getInstance());

        if (srcImageFiles == null) {
          throw new IllegalArgumentException("src.listFiles() returned \"null\"");
        }

        for (File srcImageFile : srcImageFiles) {

          Calendar c = Calendar.getInstance();
          c.setTime(new SimpleDateFormat(pattern).parse(srcImageFile.getName()));

          if (correction != null) {
            c.add(Calendar.HOUR_OF_DAY, Integer.parseInt(correction));
          }

          ImageMetadataHandler.getInstance().setDateTimeOriginal(srcImageFile, c.getTime());
        }
      }

      else {
        throw new IllegalArgumentException("illegal input");
      }
    }

    catch (Throwable e) {
      printHelp("-image -" + OPTION_METADATA + " -set -DateTimeOriginalFromFileName", options, e);
    }
  }

}
