  /*
   * Copyright (c) 2020 - 2023 UPL Foundation
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
  
  package upl.graphic;
  
  import java.util.function.Consumer;
  
  /**
   * A string builder with built-in support for ANSI escape sequences.
   *
   * @author Nathan Fiscaletti
   */
  public class AnsiStringBuilder {
    
    /**
     * Reset options.
     */
    public enum ColorReset {
      /**
       * Resets foreground color
       */
      FG,
      /**
       * Resets background color
       */
      BG,
      /**
       * Resets foreground and background color.
       */
      ALL
    }
    
    /**
     * 4-bit foreground color values.
     */
    public enum ForegroundColor {
      
      RESET (39),
      BLACK (30),
      RED (31),
      GREEN (32),
      YELLOW (33),
      BLUE (34),
      MAGENTA (35),
      CYAN (36),
      LIGHT_GRAY (37),
      DARK_GRAY (90),
      LIGHT_RED (91),
      LIGHT_GREEN (92),
      LIGHT_YELLOW (93),
      LIGHT_BLUE (94),
      LIGHT_MAGENTA (95),
      LIGHT_CYAN (96),
      WHITE (97);
      
      protected final int value;
      
      public int getValue () {
        return value;
      }
      
      ForegroundColor (int value) {
        this.value = value;
      }
      
    }
    
    /**
     * 4-bit background color values.
     */
    public enum BackgroundColor {
      
      RESET (49),
      BLACK (40),
      RED (41),
      GREEN (42),
      YELLOW (43),
      BLUE (44),
      MAGENTA (45),
      CYAN (46),
      LIGHT_GRAY (47),
      DARK_GRAY (100),
      LIGHT_RED (101),
      LIGHT_GREEN (102),
      LIGHT_YELLOW (103),
      LIGHT_BLUE (104),
      LIGHT_MAGENTA (105),
      LIGHT_CYAN (106),
      WHITE (107);
      
      protected final int value;
      
      public int getValue () {
        return value;
      }
      
      BackgroundColor (int value) {
        this.value = value;
      }
      
    }
    
    /**
     * Internal string builder wrapped by this class.
     */
    protected final StringBuilder internal;
    
    /**
     * Constructs a string builder with no characters in it and an initial
     * capacity of 16 characters.
     */
    public AnsiStringBuilder () {
      internal = new StringBuilder ();
    }
    
    /**
     * Constructs an ansi string builder that contains the same
     * characters as the specified CharSequence.
     *
     * @param charSequence
     */
    public AnsiStringBuilder (CharSequence charSequence) {
      internal = new StringBuilder (charSequence);
    }
    
    /**
     * Constructs an ansi string builder with no characters in it and an
     * initial capacity specified by the capacity argument.
     *
     * @param capacity
     */
    public AnsiStringBuilder (int capacity) {
      internal = new StringBuilder (capacity);
    }
    
    /**
     * Constructs an ansi string builder initialized to the contents of
     * the specified string.
     *
     * @param string
     */
    public AnsiStringBuilder (String string) {
      internal = new StringBuilder (string);
    }
    
    /**
     * Resets the hidden flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder resetHidden () {
      return ansi (Integer.toString (28));
    }
    
    /**
     * Resets the hidden flag and appends the string representation of the value.
     *
     * @param value
     * @return AnsiStringBuilder
     */
    public <T> AnsiStringBuilder resetHidden (T value) {
      
      resetHidden ();
      append (value);
      
      return this;
      
    }
    
    /**
     * Resets the invert-colors flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder resetInvertColors () {
      return ansi (Integer.toString (27));
    }
    
    /**
     * Resets the invert-colors flag and appends the string representation of the value.
     *
     * @param value
     * @return AnsiStringBuilder
     */
    public <T> AnsiStringBuilder resetInvertColors (T value) {
      resetInvertColors ();
      append (value);
      return this;
    }
    
    /**
     * Resets the blink flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder resetBlink () {
      return ansi (Integer.toString (25));
    }
    
    /**
     * Resets the blink flag and appends the string representation of the value.
     *
     * @param value
     * @return AnsiStringBuilder
     */
    public <T> AnsiStringBuilder resetBlink (T value) {
      resetBlink ();
      append (value);
      return this;
    }
    
    /**
     * Resets the underline flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder resetUnderline () {
      return ansi (Integer.toString (24));
    }
    
    /**
     * Resets the underline flag and appends the string representation of the value.
     *
     * @param value
     * @return AnsiStringBuilder
     */
    public <T> AnsiStringBuilder resetUnderline (T value) {
      resetUnderline ();
      append (value);
      return this;
    }
    
    /**
     * Resets the strike through flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder resetStrikeThrough () {
      return ansi (Integer.toString (29));
    }
    
    /**
     * Resets the strike-through flag and appends the string representation of the value.
     *
     * @param value
     * @return AnsiStringBuilder
     */
    public <T> AnsiStringBuilder resetStrikeThrough (T value) {
      resetStrikeThrough ();
      append (value);
      return this;
    }
    
    /**
     * Resets the dim flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder resetDim () {
      return ansi (Integer.toString (22));
    }
    
    /**
     * Resets the dim flag and appends the string representation of the value.
     *
     * @param value
     * @return AnsiStringBuilder
     */
    public <T> AnsiStringBuilder resetDim (T value) {
      resetDim ();
      append (value);
      return this;
    }
    
    /**
     * Resets the bold flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder resetBold () {
      return ansi (Integer.toString (22));
    }
    
    /**
     * Resets the bold flag and appends the string representation of the value.
     *
     * @param value
     * @return AnsiStringBuilder
     */
    public <T> AnsiStringBuilder resetBold (T value) {
      resetBold ();
      append (value);
      return this;
    }
    
    /**
     * Resets the italic flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder resetItalic () {
      return ansi (Integer.toString (23));
    }
    
    /**
     * Resets the italic flag and appends the string representation of the value.
     *
     * @param value
     * @return AnsiStringBuilder
     */
    public <T> AnsiStringBuilder resetItalic (T value) {
      resetItalic ();
      append (value);
      return this;
    }
    
    /**
     * Resets the formatting to default.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder reset () {
      return ansi (Integer.toString (0));
    }
    
    /**
     * Resets the formatting to default and appends the string representation of the value.
     *
     * @param value
     * @return AnsiStringBuilder
     */
    public <T> AnsiStringBuilder reset (T value) {
      reset ();
      append (value);
      return this;
    }
    
    /**
     * Set the hide flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder hide () {
      return ansi (Integer.toString (8));
    }
    
    /**
     * Appends the string representation of the value formatted with hide.
     *
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #hide()}
     * @see {@link #resetHidden()}
     */
    public <T> AnsiStringBuilder hide (T value) {
      hide ();
      append (value);
      resetHidden ();
      return this;
    }
    
    /**
     * Set the strike through flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder strikeThrough () {
      return ansi (Integer.toString (9));
    }
    
    /**
     * Appends the string representation of the value formatted with strike-through.
     *
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #strikeThrough()}
     * @see {@link #resetStrikeThrough()}
     */
    public <T> AnsiStringBuilder strikeThrough (T value) {
      strikeThrough ();
      append (value);
      resetStrikeThrough ();
      return this;
    }
    
    /**
     * Sets the strike-through flag, runs the function consumer on this string builder and resets
     * the strike-through flag once finished.
     *
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder strikeThrough (Consumer<AnsiStringBuilder> consumer) {
      strikeThrough ();
      consumer.accept (this);
      resetStrikeThrough ();
      return this;
    }
    
    /**
     * Set the invert color flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder invertColor () {
      return ansi (Integer.toString (7));
    }
    
    /**
     * Appends the string representation of the value formatted with inverted colors.
     *
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #invertColor()}
     * @see {@link #resetInvertColors()}
     */
    public <T> AnsiStringBuilder invertColor (T value) {
      invertColor ();
      append (value);
      return this;
    }
    
    /**
     * Sets the invert-colors flag, runs the function consumer on this string builder and resets
     * the invert-colors flag once finished.
     *
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder invertColor (Consumer<AnsiStringBuilder> consumer) {
      invertColor ();
      consumer.accept (this);
      resetInvertColors ();
      return this;
    }
    
    /**
     * Set the blink flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder blink () {
      return ansi (Integer.toString (5));
    }
    
    /**
     * Appends the string representation of the value formatted with blink.
     *
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #blink()}
     * @see {@link #resetBlink()}
     */
    public <T> AnsiStringBuilder blink (T value) {
      blink ();
      append (value);
      resetBlink ();
      return this;
    }
    
    /**
     * Sets the blink flag, runs the function consumer on this string builder and resets the blink
     * flag once finished.
     *
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder blink (Consumer<AnsiStringBuilder> consumer) {
      blink ();
      consumer.accept (this);
      resetBlink ();
      return this;
    }
    
    /**
     * Set the underline flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder underline () {
      return ansi (Integer.toString (4));
    }
    
    /**
     * Sets the underline flag, runs the function consumer on this string builder and resets the
     * underline flag once finished.
     *
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder underline (Consumer<AnsiStringBuilder> consumer) {
      underline ();
      consumer.accept (this);
      resetUnderline ();
      return this;
    }
    
    /**
     * Appends the string representation of the value formatted with underline.
     *
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #underline()}
     * @see {@link #resetUnderline()}
     */
    public <T> AnsiStringBuilder underline (T value) {
      underline ();
      append (value);
      resetUnderline ();
      return this;
    }
    
    /**
     * Set the dim flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder dim () {
      return ansi (Integer.toString (2));
    }
    
    /**
     * Appends the string representation of the value with dim formatting.
     *
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #dim()}
     * @see {@link #resetDim()}
     */
    public <T> AnsiStringBuilder dim (T value) {
      dim ();
      append (value);
      resetDim ();
      return this;
    }
    
    /**
     * Sets the dim flag, runs the function consumer on this string builder and resets the dim
     * flag once finished.
     *
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder dim (Consumer<AnsiStringBuilder> consumer) {
      dim ();
      consumer.accept (this);
      resetDim ();
      return this;
    }
    
    /**
     * Set the bold flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder bold () {
      return ansi (Integer.toString (1));
    }
    
    /**
     * Appends the string representation of the value with bold formatting.
     *
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #bold()}
     * @see {@link #resetBold()}
     */
    public <T> AnsiStringBuilder bold (T value) {
      bold ();
      append (value);
      resetBold ();
      return this;
    }
    
    /**
     * Sets the bold flag, runs the function consumer on this string builder and resets the bold
     * flag once finished.
     *
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder bold (Consumer<AnsiStringBuilder> consumer) {
      bold ();
      consumer.accept (this);
      resetBold ();
      return this;
    }
    
    /**
     * Set the italic flag.
     *
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder italic () {
      return ansi (Integer.toString (3));
    }
    
    /**
     * Appends the string representation of the value with italic formatting.
     *
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #italic()}
     * @see {@link #resetItalic()}
     */
    public <T> AnsiStringBuilder italic (T value) {
      italic ();
      append (value);
      resetItalic ();
      return this;
    }
    
    /**
     * Sets the italic flag, runs the function consumer on this string builder and resets the italic
     * flag once finished.
     *
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder italic (Consumer<AnsiStringBuilder> consumer) {
      italic ();
      consumer.accept (this);
      resetItalic ();
      return this;
    }
    
    /**
     * Sets a 4-bit foreground color.
     *
     * @param color
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder color (ForegroundColor color) {
      return ansi (Integer.toString (color.getValue ()));
    }
    
    /**
     * Sets the 4-bit foreground color, runs the function consumer on this string builder and resets
     * the foreground color once finished.
     *
     * @param color
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder color (ForegroundColor color, Consumer<AnsiStringBuilder> consumer) {
      color (color);
      consumer.accept (this);
      resetColor (ColorReset.FG);
      return this;
    }
    
    /**
     * Sets a 4-bit background color.
     *
     * @param color
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder color (BackgroundColor color) {
      return ansi (Integer.toString (color.getValue ()));
    }
    
    /**
     * Sets the 4-bit background color, runs the function consumer on this string builder and resets
     * the background color once finished.
     *
     * @param color
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder color (BackgroundColor color, Consumer<AnsiStringBuilder> consumer) {
      
      color (color);
      consumer.accept (this);
      resetColor (ColorReset.BG);
      
      return this;
      
    }
    
    /**
     * Appends the string representation of the value formatted with the
     * specified 4-bit foreground color.
     *
     * @param color
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #color(ForegroundColor)}
     * @see {@link #resetColor(ColorReset)}
     */
    public <T> AnsiStringBuilder color (ForegroundColor color, T value) {
      color (color);
      append (value);
      resetColor (ColorReset.FG);
      return this;
    }
    
    /**
     * Appends the string representation of the value formatted with the
     * specified 4-bit background color.
     *
     * @param color
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #color(BackgroundColor)}
     * @see {@link #resetColor(ColorReset)}
     */
    public <T> AnsiStringBuilder color (BackgroundColor color, T value) {
      color (color);
      append (value);
      resetColor (ColorReset.BG);
      return this;
    }
    
    /**
     * Sets a 4-bit foreground and background color.
     *
     * @param fg
     * @param fg
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder color (ForegroundColor fg, BackgroundColor bg) {
      color (fg);
      color (bg);
      return this;
    }
    
    /**
     * Sets the 4-bit foreground and background colors, runs the function consumer on this string
     * builder and resets the foreground and background colors once finished.
     *
     * @param fg
     * @param bg
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder color (ForegroundColor fg, BackgroundColor bg, Consumer<AnsiStringBuilder> consumer) {
      color (fg);
      color (bg);
      consumer.accept (this);
      resetColor (ColorReset.ALL);
      return this;
    }
    
    /**
     * Appends the string representation of the value formatted with the
     * specified 4-bit foreground and background color.
     *
     * @param fg
     * @param bg
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #color(ForegroundColor)}
     * @see {@link #color(BackgroundColor)}
     * @see {@link #resetColor(ColorReset)}
     */
    public <T> AnsiStringBuilder color (ForegroundColor fg, BackgroundColor bg, T value) {
      color (fg);
      color (bg);
      append (value);
      resetColor (ColorReset.ALL);
      return this;
    }
    
    /**
     * Resets the color.
     *
     * @param reset
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder resetColor (ColorReset reset) {
      
      switch (reset) {
        
        case FG:
          color (ForegroundColor.RESET);
          break;
          
        case BG:
          color (BackgroundColor.RESET);
          break;
          
        case ALL:
          color (ForegroundColor.RESET);
          color (BackgroundColor.RESET);
          break;
          
      }
      
      return this;
      
    }
    
    /**
     * Sets an 8-bit foreground color.
     *
     * @param color
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder color8 (int color) {
      
      if (color < 0 || color > 255)
        throw new IllegalArgumentException ("Valid 8-bit colors must be within the range of 0-255.");
      
      return ansi ("38;5;" + color);
      
    }
    
    /**
     * Sets the 8-bit foreground color, runs the function consumer on this string builder and
     * resets the foreground colors once finished.
     *
     * @param color
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder color8 (int color, Consumer<AnsiStringBuilder> consumer) {
      color8 (color);
      consumer.accept (this);
      resetColor (ColorReset.FG);
      return this;
    }
    
    /**
     * Appends the string representation of the value formatted with the
     * specified 8-bit foreground color.
     *
     * @param color
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #color8(int)}
     * @see {@link #resetColor(ColorReset)}
     */
    public <T> AnsiStringBuilder color8 (int color, T value) {
      color8 (color);
      append (value);
      resetColor (ColorReset.FG);
      return this;
    }
    
    /**
     * Sets an 8-bit background color.
     *
     * @param color
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder backgroundColor8 (int color) {
      if (color < 0 || color > 255) {
        throw new IllegalArgumentException ("Valid 8-bit colors must be within the range of 0-255.");
      }
      
      return ansi ("48;5;" + color);
    }
    
    /**
     * Sets the 8-bit background color, runs the function consumer on this string builder and
     * resets the background colors once finished.
     *
     * @param color
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder backgroundColor8 (int color, Consumer<AnsiStringBuilder> consumer) {
      backgroundColor8 (color);
      consumer.accept (this);
      resetColor (ColorReset.BG);
      return this;
    }
    
    /**
     * Appends the string representation of the value formatted with the
     * specified 8-bit background color.
     *
     * @param color
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #backgroundColor8(int)}
     * @see {@link #resetColor(ColorReset)}
     */
    public <T> AnsiStringBuilder backgroundColor8 (int color, T value) {
      backgroundColor8 (color);
      append (value);
      resetColor (ColorReset.BG);
      return this;
    }
    
    /**
     * Sets a 24-bit foreground color.
     *
     * @param r
     * @param g
     * @param b
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder color24 (int r, int g, int b) {
      if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
        throw new IllegalArgumentException ("Valid 24-bit RGB values must be within the range of 0-255.");
      }
      
      return ansi ("38;2;" + r + ";" + g + ";" + b);
    }
    
    /**
     * Sets a 24-bit foreground color using a hexadecimal color value.
     * <p>
     * Example: sb.color24('#ffffff')
     *
     * @param hexColor
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder color24 (String hexColor) {
      if (!hexColor.matches ("^\\#[A-Fa-f0-9]{6}$")) {
        throw new IllegalArgumentException ("Invalid 24-bit hexadecimal color value.");
      }
      
      Integer r = Integer.valueOf (hexColor.substring (1, 3), 16);
      Integer g = Integer.valueOf (hexColor.substring (3, 5), 16);
      Integer b = Integer.valueOf (hexColor.substring (5, 7), 16);
      
      return color24 (r, g, b);
    }
    
    /**
     * Sets the 24-bit foreground color, runs the function consumer on this string builder and
     * resets the foreground colors once finished.
     *
     * @param r
     * @param g
     * @param b
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder color24 (int r, int g, int b, Consumer<AnsiStringBuilder> consumer) {
      color24 (r, g, b);
      consumer.accept (this);
      resetColor (ColorReset.FG);
      return this;
    }
    
    /**
     * Sets the 24-bit foreground color using a hexadecimal color value, runs the function consumer
     * on this string builder and resets the foreground colors once finished.
     *
     * @param hexColor
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder color24 (String hexColor, Consumer<AnsiStringBuilder> consumer) {
      color24 (hexColor);
      consumer.accept (this);
      resetColor (ColorReset.FG);
      return this;
    }
    
    /**
     * Appends the string representation of the value formatted with the
     * specified 24-bit foreground color.
     *
     * @param r
     * @param g
     * @param b
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #color24(int, int, int)}
     * @see {@link #resetColor(ColorReset)}
     */
    public <T> AnsiStringBuilder color24 (int r, int g, int b, T value) {
      color24 (r, g, b);
      append (value);
      resetColor (ColorReset.FG);
      return this;
    }
    
    /**
     * Appends the string representation of the value formatted with the
     * specified 24-bit hexadecimal foreground color.
     *
     * @param hexColor
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #color24(String)}
     * @see {@link #resetColor(ColorReset)}
     */
    public <T> AnsiStringBuilder color24 (String hexColor, T value) {
      color24 (hexColor);
      append (value);
      resetColor (ColorReset.FG);
      return this;
    }
    
    /**
     * Sets an 24-bit background color.
     *
     * @param r
     * @param g
     * @param b
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder backgroundColor24 (int r, int g, int b) {
      if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
        throw new IllegalArgumentException ("Valid 24-bit colors must be within the range of 0-255.");
      }
      
      return ansi ("48;2;" + r + ";" + g + ";" + b);
    }
    
    /**
     * Sets an 24-bit background color using a hexadecimal color value.
     *
     * @param hexColor
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder backgroundColor24 (String hexColor) {
      if (!hexColor.matches ("^\\#[A-Fa-f0-9]{6}$")) {
        throw new IllegalArgumentException ("Invalid 24-bit hexadecimal color value.");
      }
      
      Integer r = Integer.valueOf (hexColor.substring (1, 3), 16);
      Integer g = Integer.valueOf (hexColor.substring (3, 5), 16);
      Integer b = Integer.valueOf (hexColor.substring (5, 7), 16);
      
      return backgroundColor24 (r, g, b);
    }
    
    /**
     * Sets the 24-bit background color, runs the function consumer on this string builder and
     * resets the background colors once finished.
     *
     * @param r
     * @param g
     * @param b
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder backgroundColor24 (int r, int g, int b, Consumer<AnsiStringBuilder> consumer) {
      backgroundColor24 (r, g, b);
      consumer.accept (this);
      resetColor (ColorReset.BG);
      return this;
    }
    
    /**
     * Sets the 24-bit background color using a hexadecimal color value, runs the function consumer
     * on this string builder and resets the background colors once finished.
     *
     * @param hexColor
     * @param consumer
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder backgroundColor24 (String hexColor, Consumer<AnsiStringBuilder> consumer) {
      backgroundColor24 (hexColor);
      consumer.accept (this);
      resetColor (ColorReset.BG);
      return this;
    }
    
    /**
     * Appends the string representation of the value formatted with the
     * specified 24-bit background color.
     *
     * @param r
     * @param g
     * @param b
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #backgroundColor24(int, int, int)}
     * @see {@link #resetColor(ColorReset)}
     */
    public <T> AnsiStringBuilder backgroundColor24 (int r, int g, int b, T value) {
      backgroundColor24 (r, g, b);
      append (value);
      resetColor (ColorReset.BG);
      return this;
    }
    
    /**
     * Appends the string representation of the value formatted with the
     * specified 24-bit hexadecimal background color.
     *
     * @param hexColor
     * @param value
     * @return AnsiStringBuilder
     * @see {@link #backgroundColor24(String)}
     * @see {@link #resetColor(ColorReset)}
     */
    public <T> AnsiStringBuilder backgroundColor24 (String hexColor, T value) {
      backgroundColor24 (hexColor);
      append (value);
      resetColor (ColorReset.BG);
      return this;
    }
    
    /**
     * Appends a custom ANSI flag.
     *
     * @param value
     * @return AnsiStringBuilder
     */
    public AnsiStringBuilder ansi (String value) {
      append ("\u001B[" + value + "m");
      return this;
    }
    
    /**
     * Appends the string representation of the value argument.
     *
     * @param value
     * @return AnsiStringBuilder
     * @see {@link StringBuilder#append(Object))}
     */
    public <T> AnsiStringBuilder append (T value) {
      internal.append (value);
      return this;
    }
    
    /**
     * Same as calling toString(true)
     *
     * @return String
     * @see {@link #toString(boolean)}
     */
    @Override
    public String toString () {
      return toString (true);
    }
    
    /**
     * Returns a string representing the data in this sequence.
     *
     * @param reset If true, a final call to {@link #reset()} will be made
     *              before preparing the string.
     * @return String
     */
    public String toString (boolean reset) {
      return toString (reset, false);
    }
    
    /**
     * Returns a string representing the data in this sequence.
     *
     * @param reset If true, a final call to {@link #reset()} will be made
     *              before preparing the string.
     * @param strip If true, the result will have all ANSI escape sequences
     *              removed before returning to caller.
     * @return String
     */
    public String toString (boolean reset, boolean strip) {
      if (reset) {
        reset ();
      }
      
      String result = internal.toString ();
      if (strip) {
        result = result.replaceAll ("\u001B\\[[;\\d]*[ -/]*[@-~]", "");
      }
      
      return result;
    }
    
  }