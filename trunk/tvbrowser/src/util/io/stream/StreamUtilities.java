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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.net.URL;
import java.net.URLConnection;

import util.io.IOUtilities;

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

  interface IInputStreamMethod {
    InputStream openInputStream() throws IOException;
  }

  interface IOutputStreamMethod {
    OutputStream openOutputStream() throws IOException;
  }

  private static void inputStream(final IInputStreamMethod inputMethod,
      final InputStreamProcessor processor)
      throws IOException {
    IOException processException = null;
    InputStream input = null;
    BufferedInputStream bufferedStream = null;
    try {
      input = inputMethod.openInputStream();
      bufferedStream = new BufferedInputStream(input);
      processor.process(bufferedStream);
    } catch (IOException e) {
      processException = e;
    } finally {
      // close stream
      if (bufferedStream != null) {
        try {
          bufferedStream.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          } else {
            processException = e;
          }
        }
      }
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
   * Lets you work with a file based input stream. It is guaranteed that the
   * underlying stream is closed, even if IOExceptions occur (which are still
   * thrown further to the caller of this method)
   *
   * @param file
   * @param processor
   * @throws IOException
   * @since 3.0
   */
  public static void inputStream(final File file,
      final InputStreamProcessor processor)
      throws IOException {
    inputStream(new IInputStreamMethod() {

      @Override
      public InputStream openInputStream() throws IOException {
        return new FileInputStream(file);
      }
    }, processor);
  }

  /**
   * Lets you work with a URL based input stream. It is guaranteed that the
   * underlying stream is closed, even if IOExceptions occur (which are still
   * thrown further to the caller of this method)
   *
   * @param urlConnection
   * @param processor
   * @throws IOException
   * @since 3.0
   */
  public static void inputStream(final URLConnection urlConnection,
      final InputStreamProcessor processor)
      throws IOException {
    inputStream(new IInputStreamMethod() {

      @Override
      public InputStream openInputStream() throws IOException {
        return urlConnection.getInputStream();
      }
    }, processor);
  }

  /**
   * Lets you work with a generic input stream. It is guaranteed that the
   * stream is closed after the method, even if IOExceptions occur (which are still
   * thrown further to the caller of this method)
   *
   * @param inputStream
   * @param processor
   * @throws IOException
   * @since 3.0
   */
  public static void inputStream(final InputStream inputStream,
      final InputStreamProcessor processor)
      throws IOException {
    inputStream(new IInputStreamMethod() {

      @Override
      public InputStream openInputStream() throws IOException {
        return inputStream;
      }
    }, processor);
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
  public static void inputStreamIgnoringExceptions(final String fileName,
      final InputStreamProcessor processor) {
    inputStreamIgnoringExceptions(new File(fileName), processor);
  }

  public static void inputStreamIgnoringExceptions(final File file,
      final InputStreamProcessor processor) {
    try {
      inputStream(file, processor);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void inputStream(final String fileName,
      final InputStreamProcessor processor)
      throws IOException {
    inputStream(new File(fileName), processor);
  }

  /**
   * Lets you work with a file based input stream. It is guaranteed that the
   * underlying stream is closed, even if IOExceptions occur (which are still
   * thrown further to the caller of this method)
   *
   * @param url URL to open
   * @param processor
   * @throws IOException
   * @since 3.0
   */
  public static void inputStream(final URL url,
      final InputStreamProcessor processor)
      throws IOException {
    inputStream(new IInputStreamMethod() {

      @Override
      public InputStream openInputStream() throws IOException {
        return IOUtilities.getStream(url);
      }
    }, processor);
  }

  public static void bufferedReader(final File file,
      final BufferedReaderProcessor processor)
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

  public static void bufferedReaderIgnoringExceptions(final File file,
      final BufferedReaderProcessor processor) {
    try {
      bufferedReader(file, processor);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void bufferedReader(final String fileName,
      final BufferedReaderProcessor processor) throws IOException {
    bufferedReader(new File(fileName), processor);
  }

  private static void outputStream(final IOutputStreamMethod outputMethod,
      final OutputStreamProcessor processor)
      throws IOException {
    IOException processException = null;
    OutputStream output = null;
    BufferedOutputStream bufferedStream = null;
    try {
      output = outputMethod.openOutputStream();
      bufferedStream = new BufferedOutputStream(output);
      processor.process(bufferedStream);
    } catch (IOException e) {
      processException = e;
    } finally {
      // close stream
      if (bufferedStream != null) {
        try {
          bufferedStream.close();
        } catch (IOException e) {
          if (processException != null) {
            processException = new IOException(processException);
          } else {
            processException = e;
          }
        }
      }
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

  public static void outputStream(final File file,
      final OutputStreamProcessor processor)
      throws IOException {
    outputStream(new IOutputStreamMethod() {

      @Override
      public OutputStream openOutputStream() throws IOException {
        return new FileOutputStream(file);
      }
    }, processor);
  }

  public static void outputStream(final String fileName,
      final OutputStreamProcessor processor) throws IOException {
    outputStream(new File(fileName), processor);
  }

  public static void objectOutputStream(final File file,
      final ObjectOutputStreamProcessor processor) throws IOException {
    IOException processException = null;
    ObjectOutputStream objectStream = null;
    FileOutputStream fileStream = null;
    BufferedOutputStream bufferedStream = null;
    try {
      fileStream = new FileOutputStream(file);
      bufferedStream = new BufferedOutputStream(fileStream);
      objectStream = new ObjectOutputStream(bufferedStream);
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
      if (bufferedStream != null) {
        try {
          bufferedStream.close();
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

  public static void objectOutputStreamIgnoringExceptions(final File file,
      ObjectOutputStreamProcessor processor) {
    try {
      objectOutputStream(file, processor);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void bufferedWriter(final File file,
      final BufferedWriterProcessor processor)
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

  public static void bufferedWriterIgnoringExceptions(final File file,
      final BufferedWriterProcessor processor) {
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
  public static void objectInputStream(final File file,
      final ObjectInputStreamProcessor processor) throws IOException {
    objectInputStream(file, 0, processor);
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
  public static void objectInputStream(final File file, final int bufferSize,
      final ObjectInputStreamProcessor processor) throws IOException {
    IOException processException = null;
    ObjectInputStream objectStream = null;
    FileInputStream fileStream = null;
    BufferedInputStream bufferedStream = null;
    try {
      fileStream = new FileInputStream(file);
      if (bufferSize > 0) {
        bufferedStream = new BufferedInputStream(fileStream, bufferSize);
      } else {
        bufferedStream = new BufferedInputStream(fileStream);
      }
      objectStream = new ObjectInputStream(bufferedStream);
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
      if (bufferedStream != null) {
        try {
          bufferedStream.close();
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

  public static void objectInputStreamIgnoringExceptions(final File file,
      final ObjectInputStreamProcessor processor) {
    objectInputStreamIgnoringExceptions(file, 0, processor);
  }

  public static void objectInputStreamIgnoringExceptions(final File file,
      final int bufferSize, final ObjectInputStreamProcessor processor) {
    try {
      objectInputStream(file, bufferSize, processor);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void printStream(final File file, final boolean autoFlush,
      final String encoding, final PrintStreamProcessor processor)
      throws IOException {
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

  public static void printStream(final File file,
      final PrintStreamProcessor processor)
      throws IOException {
    printStream(file, false, null, processor);
  }


}