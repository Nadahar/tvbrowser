/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package util.io.stream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Stream processor class for working with streams
 * 
 * This class does all the IOException handling for working with streams, so you
 * can concentrate on your algorithm instead.
 * 
 * @author Bananeweizen
 * @since 3.0
 * 
 */
public class StreamUtilities {

  /**
   * Lets you work with a file based input stream. It is guaranteed that the
   * underlying stream is closed, even if IOExceptions occur (which are still
   * thrown further to the caller of this method)
   * 
   * @param file
   * @param processor
   * @throws IOException
   * @since 3.0
   */
  public static void inputStream(File file, InputStreamProcessor processor)
      throws IOException {
    IOException processException = null;
    InputStream input = null;
    try {
      input = new FileInputStream(file);
      processor.process(input);
    } catch (IOException e) {
      processException = e;
    } finally {
      // close stream
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          } else {
            processException = e;
          }
        }
      }
      if (processException != null) {
        throw processException;
      }
    }
  }

  /**
   * Lets you work with a file name based input stream. It is guaranteed that
   * the underlying stream is closed, even if IOExceptions occur (which are not
   * thrown)
   * 
   * @param fileName
   * @param processor
   * @since 3.0
   */
  public static void inputStreamIgnoringExceptions(String fileName,
      InputStreamProcessor processor) {
    inputStreamIgnoringExceptions(new File(fileName), processor);
  }

  public static void inputStreamIgnoringExceptions(File file,
      InputStreamProcessor processor) {
    try {
      inputStream(file, processor);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void inputStream(String fileName, InputStreamProcessor processor)
      throws IOException {
    inputStream(new File(fileName), processor);
  }

  public static void bufferedReader(File file, BufferedReaderProcessor processor)
      throws IOException {
    FileReader fileReader = null;
    BufferedReader bufferedReader = null;
    IOException processException = null;
    try {
      fileReader = new FileReader(file);
      bufferedReader = new BufferedReader(fileReader);
      processor.process(bufferedReader);
    } catch (IOException e) {
      processException = e;
    } finally {
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          }
          else {
            processException = e;
          }
        }
      }
      if (fileReader != null) {
        try {
          fileReader.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          }
          else {
            processException = e;
          }
        }
      }
      if (processException != null) {
        throw processException;
      }
    }
  }

  public static void bufferedReaderIgnoringExceptions(File file,
      BufferedReaderProcessor processor) {
    try {
      bufferedReader(file, processor);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void bufferedReader(String fileName,
      BufferedReaderProcessor processor) throws IOException {
    bufferedReader(new File(fileName), processor);
  }

  public static void outputStream(File file, OutputStreamProcessor processor)
      throws IOException {
    IOException processException = null;
    OutputStream output = null;
    try {
      output = new FileOutputStream(file);
      processor.process(output);
    } catch (IOException e) {
      processException = e;
    } finally {
      // close stream
      if (output != null) {
        try {
          output.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          } else {
            processException = e;
          }
        }
      }
      if (processException != null) {
        throw processException;
      }
    }
  }

  public static void outputStream(String fileName,
      OutputStreamProcessor processor) throws IOException {
    outputStream(new File(fileName), processor);
  }

  public static void objectOutputStream(File file,
      ObjectOutputStreamProcessor processor) throws IOException {
    IOException processException = null;
    ObjectOutputStream objectStream = null;
    FileOutputStream fileStream = null;
    try {
      fileStream = new FileOutputStream(file);
      objectStream = new ObjectOutputStream(fileStream);
      processor.process(objectStream);
    } catch (IOException e) {
      processException = e;
    } finally {
      // close stream
      if (objectStream != null) {
        try {
          objectStream.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          } else {
            processException = e;
          }
        }
      }
      if (fileStream != null) {
        try {
          fileStream.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          } else {
            processException = e;
          }
        }
      }
      if (processException != null) {
        throw processException;
      }
    }
  }

  public static void objectOutputStreamIgnoringExceptions(File file,
      ObjectOutputStreamProcessor processor) {
    try {
      objectOutputStream(file, processor);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void bufferedWriter(File file, BufferedWriterProcessor processor)
      throws IOException {
    FileWriter fileWriter = null;
    BufferedWriter bufferedWriter = null;
    IOException processException = null;
    try {
      fileWriter = new FileWriter(file);
      bufferedWriter = new BufferedWriter(fileWriter);
      processor.process(bufferedWriter);
    } catch (IOException e) {
      processException = e;
    } finally {
      if (bufferedWriter != null) {
        try {
          bufferedWriter.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          }
          else {
            processException = e;
          }
        }
      }
      if (fileWriter != null) {
        try {
          fileWriter.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          }
          else {
            processException = e;
          }
        }
      }
      if (processException != null) {
        throw processException;
      }
    }
  }

  public static void bufferedWriterIgnoringExceptions(File file,
      BufferedWriterProcessor processor) {
    try {
      bufferedWriter(file, processor);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Lets you work with a file based input stream. It is guaranteed that the
   * underlying stream is closed, even if IOExceptions occur (which are still
   * thrown further to the caller of this method)
   * 
   * @param file
   * @param processor
   * @throws IOException
   * @since 3.0
   */
  public static void objectInputStream(File file,
      ObjectInputStreamProcessor processor) throws IOException {
    IOException processException = null;
    ObjectInputStream objectStream = null;
    FileInputStream fileStream = null;
    try {
      fileStream = new FileInputStream(file);
      objectStream = new ObjectInputStream(fileStream);
      processor.process(objectStream);
    } catch (IOException e) {
      processException = e;
    } finally {
      // close stream
      if (objectStream != null) {
        try {
          objectStream.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          } else {
            processException = e;
          }
        }
      }
      if (fileStream != null) {
        try {
          fileStream.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          } else {
            processException = e;
          }
        }
      }
      if (processException != null) {
        throw processException;
      }
    }
  }

  public static void objectInputStreamIgnoringExceptions(File file,
      ObjectInputStreamProcessor processor) {
    try {
      objectInputStream(file, processor);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void printStream(File file, boolean autoFlush, String encoding,
      PrintStreamProcessor processor) throws IOException {
    IOException processException = null;
    PrintStream printStream = null;
    FileOutputStream fileStream = null;
    try {
      fileStream = new FileOutputStream(file);
      if (encoding != null) {
        printStream = new PrintStream(fileStream, autoFlush, encoding);
      } else {
        printStream = new PrintStream(fileStream, autoFlush);
      }
      processor.process(printStream);
    } catch (IOException e) {
      processException = e;
    } finally {
      // close stream
      if (printStream != null) {
        printStream.close();
      }
      if (fileStream != null) {
        try {
          fileStream.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          } else {
            processException = e;
          }
        }
      }
      if (processException != null) {
        throw processException;
      }
    }
  }

  public static void printStream(File file, PrintStreamProcessor processor)
      throws IOException {
    printStream(file, false, null, processor);
  }


}