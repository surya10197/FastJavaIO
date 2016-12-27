import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.InputMismatchException;

public class InputReader {
  
  private int buffer_sz, buf_index, num_bytes_read, c;
  
  private final byte[] buf; 
  private final InputStream stream;
  
  private static final int DEFAULT_BUFFER_SZ = 5; // 65536; // 2^16
  private static final InputStream DEFAULT_STREAM = System.in;

  private static final int EOF   = -1; // End Of File character
  private static final int NL    = 10; // '\n' - New Line (NL)
  private static final int SP    = 32; // ' '  - Space character (SP)
  private static final int DASH  = 45; // '-'  - Dash character (DOT)
  private static final int DOT   = 46; // '.'  - Dot character (DOT)
  
  // double lookup table, used for optimizations.
  private static final double[][] doubles = {
    { 0.0d,0.00d,0.000d,0.0000d,0.00000d,0.000000d,0.0000000d,0.00000000d,0.000000000d,0.0000000000d,0.00000000000d,0.000000000000d,0.0000000000000d,0.00000000000000d,0.000000000000000d},
    { 0.1d,0.01d,0.001d,0.0001d,0.00001d,0.000001d,0.0000001d,0.00000001d,0.000000001d,0.0000000001d,0.00000000001d,0.000000000001d,0.0000000000001d,0.00000000000001d,0.000000000000001d},        
    { 0.2d,0.02d,0.002d,0.0002d,0.00002d,0.000002d,0.0000002d,0.00000002d,0.000000002d,0.0000000002d,0.00000000002d,0.000000000002d,0.0000000000002d,0.00000000000002d,0.000000000000002d},        
    { 0.3d,0.03d,0.003d,0.0003d,0.00003d,0.000003d,0.0000003d,0.00000003d,0.000000003d,0.0000000003d,0.00000000003d,0.000000000003d,0.0000000000003d,0.00000000000003d,0.000000000000003d},        
    { 0.4d,0.04d,0.004d,0.0004d,0.00004d,0.000004d,0.0000004d,0.00000004d,0.000000004d,0.0000000004d,0.00000000004d,0.000000000004d,0.0000000000004d,0.00000000000004d,0.000000000000004d},        
    { 0.5d,0.05d,0.005d,0.0005d,0.00005d,0.000005d,0.0000005d,0.00000005d,0.000000005d,0.0000000005d,0.00000000005d,0.000000000005d,0.0000000000005d,0.00000000000005d,0.000000000000005d},        
    { 0.6d,0.06d,0.006d,0.0006d,0.00006d,0.000006d,0.0000006d,0.00000006d,0.000000006d,0.0000000006d,0.00000000006d,0.000000000006d,0.0000000000006d,0.00000000000006d,0.000000000000006d},        
    { 0.7d,0.07d,0.007d,0.0007d,0.00007d,0.000007d,0.0000007d,0.00000007d,0.000000007d,0.0000000007d,0.00000000007d,0.000000000007d,0.0000000000007d,0.00000000000007d,0.000000000000007d},        
    { 0.8d,0.08d,0.008d,0.0008d,0.00008d,0.000008d,0.0000008d,0.00000008d,0.000000008d,0.0000000008d,0.00000000008d,0.000000000008d,0.0000000000008d,0.00000000000008d,0.000000000000008d},        
    { 0.9d,0.09d,0.009d,0.0009d,0.00009d,0.000009d,0.0000009d,0.00000009d,0.000000009d,0.0000000009d,0.00000000009d,0.000000000009d,0.0000000000009d,0.00000000000009d,0.000000000000009d}
  };

  public InputReader () { this(DEFAULT_STREAM, DEFAULT_BUFFER_SZ); }
  public InputReader (InputStream stream) { this(stream, DEFAULT_BUFFER_SZ); }
  public InputReader (int buffer_sz) { this(DEFAULT_STREAM, buffer_sz); }

  // Designated constructor
  public InputReader (InputStream stream, int buffer_sz) {
    if (stream == null || buffer_sz <= 0) throw new IllegalArgumentException();
    buf = new byte[buffer_sz];
    this.buffer_sz = buffer_sz;
    this.stream = stream;
  }

  // Reads a single character from input
  // returns the byte value of the next character in the buffer.
  // Also returns -1 if there is no more data to read
  public int read() throws IOException {

    if (num_bytes_read == EOF) throw new InputMismatchException();

    if (buf_index >= num_bytes_read) {
      buf_index = 0;
      num_bytes_read = stream.read(buf);
      if (num_bytes_read == EOF)
        return EOF;
    }
    return buf[buf_index++];

  }

  // Reads a 32bit signed integer from input stream
  public int readInt() throws IOException {
    c = read(); int sgn = 1, res = 0;
    while (c <= SP) c = read(); // while c is either: ' ', '\n', EOF
    if (c == DASH) { sgn = -1; c = read(); }
    do { res = (res<<3)+(res<<1); res += c - '0'; c = read(); }
    while (c > SP); // Still has digits
    return res * sgn;
  }

  // Reads a 64bit signed integer from input stream
  public long readLong() throws IOException {
    c = read();
    while (c <= SP) c = read(); // while c is either: ' ' or '\n'
    int sgn = 1;
    if (c == DASH) { sgn = -1; c = read(); }
    long res = 0;
    do { res = (res<<3)+(res<<1); res += c - '0'; c = read(); }
    while (c > SP); // Still has digits
    return res * sgn; 
  }

  // Reads everything in the input stream into a string
  public String readAll() throws IOException {

    if (num_bytes_read == EOF) return null;

    ByteArrayOutputStream result = new ByteArrayOutputStream(buffer_sz);

    // Finish writing data currently in the buffer
    result.write(buf, buf_index, num_bytes_read - buf_index);

    // Write data until into the result output stream until there is no more
    while ( (num_bytes_read = stream.read(buf)) != EOF)
      result.write(buf, 0, num_bytes_read);
      
    return result.toString("UTF-8");

  }

  // public String readLine() throws IOException {
    
  //   if (num_bytes_read == -1) return null;

  //   int start = buf_index;
  //   ByteArrayOutputStream result = new ByteArrayOutputStream();

    // while (true) {

    //   while( buf_index < num_bytes_read && buf[buf_index] != 10 ) buf_index++;
    //   if (buf_index == 10) return result.toString();

    //   result.write(buf, start, buf_index - start );      

    // }

    // Finish writing data currently in the buffer
    // while( buf_index < num_bytes_read && buf[buf_index] != 10 ) buf_index++;
    // result.write(buf, start, buf_index - start );
    
    // // Keep reading we have no found the end of the string yet
    // if (buf_index == buffer_sz) {
    //   buf_index = 0;

    // }


    // // While we have not reached the end
    // while ((num_bytes_read = stream.read(buf)) != -1) {
    //   int i = 0;
    //   while( i < num_bytes_read && buf[i] != 10 ) i++;
    //   result.write(b, 0, i);
    //   if (i < num_bytes_read) break; // Found a '\n' character
    //   // System.out.println(java.util.Arrays.toString(b));
    //   // System.out.println(result.toString("UTF-8"));
    // }

    // return result.toString();

  // }

  // Reads a line from input stream.
  // Returns null if there are no more lines
  public String readLine() throws IOException {
    try { c=read(); } catch (InputMismatchException e) {return null; }
    if (c == NL) return ""; // Empty line
    if (c == EOF) return null; // EOF
    StringBuilder res = new StringBuilder();
    do { res.appendCodePoint(c); c = read(); }
    while (c != NL && c != EOF); // Spaces & tabs are ok, but not newlines or EOF characters
    return res.toString();    
  }

  // Reads a string of characters from the input stream. 
  // The delimiter separating a string of characters is set to be:
  // any ASCII value <= 32 meaning any spaces, new lines, EOF, tabs ...
  public String readStr() throws IOException {
    
    c = 0;

    // while c is either: ' ' or '\n'
    try { while (c <= SP) c = read();

    // EOF throws exception
    } catch (InputMismatchException e) { return null; }
    
    StringBuilder res = new StringBuilder();
    do { res.appendCodePoint(c); c = read(); }
    while (c > SP); // Still non-space characters
    return res.toString();
  }

  // Returns an exact value a double value from the input stream.
  // This method is ~2.5x slower than readDoubleFast.
  public double readDouble() throws IOException {
    return Double.valueOf(readStr());
  }

  // Very quickly reads a double value from the input stream. However, this method only 
  // returns an approximate double value from input stream. The value is not
  // exact because we're doing arithmetic (adding, multiplication) on finite floating point numbers.
  @Deprecated public double readDoubleFast() throws IOException {
    c = read(); int sgn = 1;
    while (c <= SP) c = read(); // while c is either: ' ', '\n', EOF
    if (c == DASH) { sgn = -1; c = read(); }
    double res = 0.0;
    // while c is not: ' ', '\n', '.' or -1
    while (c > 46) {res *= 10.0; res += c - '0'; c = read(); }
    if (c == DOT) {
      int i = 0; c = read();
      // while c is digit and there are < 15 digits after dot
      while (c > SP && i < 15)
      { res += doubles[(c - '0')][i++]; c = read(); }
    }
    return res * sgn;
  }

  public static void main(String[] args) throws IOException {
    InputReader in = new InputReader();
    System.out.println(in.readLine());
    System.out.println(in.readLine());
    System.out.println(in.readLine());
    System.out.println(in.readLine());
  }

}