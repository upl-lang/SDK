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
  
  package upl.images;
  
  import java.awt.Color;
  import java.awt.Graphics;
  import java.awt.Graphics2D;
  import java.awt.RenderingHints;
  import java.awt.Transparency;
  import java.awt.color.ColorSpace;
  import java.awt.geom.AffineTransform;
  import java.awt.geom.Rectangle2D;
  import java.awt.image.BufferedImage;
  import java.awt.image.BufferedImageOp;
  import java.awt.image.ColorConvertOp;
  import java.awt.image.ConvolveOp;
  import java.awt.image.DataBufferByte;
  import java.awt.image.ImagingOpException;
  import java.awt.image.Kernel;
  import java.awt.image.RescaleOp;
  import java.io.ByteArrayInputStream;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.OutputStream;
  import javax.imageio.ImageIO;
  import upl.io.BufferedInputStream;
  import upl.util.ArrayList;
  import upl.util.List;
  
  public class Image extends BufferedImage {
    
    protected BufferedImage src;
    
    public ScalingMethod scalingMethod = ScalingMethod.AUTOMATIC;
    List<BufferedImageOp> ops = new ArrayList<> ();
    
    public Image (InputStream stream) throws IOException {
      this (ImageIO.read (stream));
    }
    
    public Image (BufferedImage src) {
      super (src.getWidth (), src.getHeight (), src.getType ());
    }
    
    public static class Options {
      
      int width = 0, height = 0, x = 0, y = 0;
      
      public Options setWidth (int width) {
        
        this.width = width;
        return this;
        
      }
      
      public Options setHeight (int height) {
        
        this.height = height;
        return this;
        
      }
      
    }
    
    public String LOG_PREFIX_PROPERTY_NAME = "imgscalr.logPrefix";
    public boolean DEBUG = false;
    public String LOG_PREFIX = System.getProperty (LOG_PREFIX_PROPERTY_NAME, "[imgscalr] ");
    public ConvolveOp OP_ANTIALIAS = new ConvolveOp (new Kernel (3, 3, new float[] {.0f, .08f, .0f, .08f, .68f, .08f, .0f, .08f, .0f}), ConvolveOp.EDGE_NO_OP, null);
    public RescaleOp OP_DARKER = new RescaleOp (0.9f, 0, null);
    public RescaleOp OP_BRIGHTER = new RescaleOp (1.1f, 0, null);
    public ColorConvertOp OP_GRAYSCALE = new ColorConvertOp (ColorSpace.getInstance (ColorSpace.CS_GRAY), null);
    
    public Image addFilter (BufferedImageOp op) {
      
      ops.put (op);
      return this;
      
    }
    
    public enum ScalingMethod {
      
      AUTOMATIC,
      BALANCED,
      QUALITY,
      ULTRA_QUALITY;
      
    }
    
    public enum ResizeMode {
      
      AUTOMATIC,
      FIT_EXACT,
      BEST_FIT_BOTH,
      FIT_WIDTH,
      FIT_HEIGHT;
      
    }
    
    public enum Rotation {
      
      CW_90,
      CW_180,
      CW_270,
      FLIP_HORIZONTAL,
      FLIP_VERTICAL;
      
    }
    
    public int THRESHOLD_BALANCED_SPEED = 1600;
    public int THRESHOLD_QUALITY_BALANCED = 800;
    
    public void apply () throws IllegalArgumentException, ImagingOpException {
      
      if (ops.length () > 0) {
        
        long t = -1;
        if (DEBUG) t = System.currentTimeMillis ();
        
        if (!(src.getType () == BufferedImage.TYPE_3BYTE_BGR || src.getType () == BufferedImage.TYPE_4BYTE_ABGR))
          src = copyToOptimalImage ();
        
        if (DEBUG) log (0, "Applying %d BufferedImageOps...", ops.length ());
        
        boolean hasReassignedSrc = false;
        
        for (BufferedImageOp op : ops) {
          
          long subT = -1;
          if (DEBUG) subT = System.currentTimeMillis ();
          
          if (DEBUG) log (1, "Applying BufferedImageOp [class=%s, toString=%s]...", op.getClass (), op.toString ());
          Rectangle2D resultBounds = op.getBounds2D (src);
          
          // Watch out for flaky/misbehaving ops that fail to work right.
          if (resultBounds == null)
            throw new ImagingOpException ("BufferedImageOp [" + op.toString () + "] getBounds2D(src) returned null bounds for the target image; this should not happen and indicates a problem with application of this type of op.");
          
          BufferedImage dest = createOptimalImage ((int) Math.round (resultBounds.getWidth ()), (int) Math.round (resultBounds.getHeight ()));
          
          // Perform the operation, update our result to return.
          BufferedImage result = op.filter (src, dest);
          if (hasReassignedSrc) src.flush ();
          
          src = result;
          
          hasReassignedSrc = true;
          
          if (DEBUG)
            log (1, "Applied BufferedImageOp in %d ms, result [width=%d, height=%d]", System.currentTimeMillis () - subT, result.getWidth (), result.getHeight ());
          
        }
        
        if (DEBUG) log (0, "All %d BufferedImageOps applied in %d ms", ops.length (), System.currentTimeMillis () - t);
        
      }
      
    }
    
    public Image crop (Options options) throws IllegalArgumentException, ImagingOpException {
      
      long t = -1;
      if (DEBUG) t = System.currentTimeMillis ();
      
      if (options.x < 0 || options.y < 0 || options.width < 0 || options.height < 0)
        throw new IllegalArgumentException ("Invalid crop bounds: x [" + options.x + "], y [" + options.y + "], width [" + options.width + "] and height [" + options.height + "] must all be >= 0");
      
      int srcWidth = src.getWidth ();
      int srcHeight = src.getHeight ();
      
      if ((options.x + options.width) > srcWidth)
        throw new IllegalArgumentException ("Invalid crop bounds: x + width [" + (options.x + options.width) + "] must be <= src.getWidth() [" + srcWidth + "]");
      if ((options.y + options.height) > srcHeight)
        throw new IllegalArgumentException ("Invalid crop bounds: y + height [" + (options.y + options.height) + "] must be <= src.getHeight() [" + srcHeight + "]");
      
      if (DEBUG)
        log (0, "Cropping Image [width=%d, height=%d] to [x=%d, y=%d, width=%d, height=%d]...", srcWidth, srcHeight, options.x, options.y, options.width, options.height);
      
      // Create a target image of an optimal type to render into.
      src = createOptimalImage (options.width, options.height);
      
      Graphics g = src.getGraphics ();
      
      g.drawImage (src, 0, 0, options.width, options.height, options.x, options.y, (options.x + options.width), (options.y + options.height), null);
      g.dispose ();
      
      if (DEBUG) log (0, "Cropped Image in %d ms", System.currentTimeMillis () - t);
      
      // Apply any optional operations (if specified).
      apply ();
      
      return this;
      
    }
    
    public Image pad (int padding) throws IllegalArgumentException, ImagingOpException {
      return pad (padding, Color.BLACK);
    }
    
    public Image pad (int padding, Color color) throws IllegalArgumentException, ImagingOpException {
      
      long t = -1;
      if (DEBUG) t = System.currentTimeMillis ();
      
      if (padding < 1) throw new IllegalArgumentException ("padding [" + padding + "] must be > 0");
      if (color == null) throw new IllegalArgumentException ("color cannot be null");
      
      int srcWidth = src.getWidth ();
      int srcHeight = src.getHeight ();
      int sizeDiff = (padding * 2);
      int newWidth = srcWidth + sizeDiff;
      int newHeight = srcHeight + sizeDiff;
      
      if (DEBUG)
        log (0, "Padding Image from [originalWidth=%d, originalHeight=%d, padding=%d] to [newWidth=%d, newHeight=%d]...", srcWidth, srcHeight, padding, newWidth, newHeight);
      
      boolean colorHasAlpha = (color.getAlpha () != 255);
      boolean imageHasAlpha = (src.getTransparency () != BufferedImage.OPAQUE);
      
      Image result;
      
      if (colorHasAlpha || imageHasAlpha) {
        
        if (DEBUG) log (1, "Transparency FOUND in source image or color, using ARGB image type...");
        
        result = new Image (new BufferedImage (newWidth, newHeight, BufferedImage.TYPE_4BYTE_ABGR));
        
      } else {
        
        if (DEBUG) log (1, "Transparency NOT FOUND in source image or color, using RGB image type...");
        
        result = new Image (new BufferedImage (newWidth, newHeight, BufferedImage.TYPE_3BYTE_BGR));
        
      }
      
      Graphics g = result.src.getGraphics ();
      
      // Draw the border of the image in the color specified.
      g.setColor (color);
      
      g.fillRect (0, 0, newWidth, padding);
      g.fillRect (0, padding, padding, newHeight);
      g.fillRect (padding, newHeight - padding, newWidth, newHeight);
      g.fillRect (newWidth - padding, padding, newWidth, newHeight - padding);
      
      // Draw the image into the center of the new padded image.
      g.drawImage (src, padding, padding, null);
      g.dispose ();
      
      if (DEBUG) log (0, "Padding Applied in %d ms", System.currentTimeMillis () - t);
      
      // Apply any optional operations (if specified).
      result.apply ();
      
      return this;
      
    }
    
    public Image resize (Options options) throws IllegalArgumentException, ImagingOpException {
      return resize (options, ResizeMode.AUTOMATIC);
    }
    
    public Image resize (Options options, ResizeMode resizeMode) throws IllegalArgumentException, ImagingOpException {
      
      long t = -1;
      if (DEBUG) t = System.currentTimeMillis ();
      
      int currentWidth = src.getWidth ();
      int currentHeight = src.getHeight ();
      
      // <= 1 is a square or landscape-oriented image, > 1 is a portrait.
      float ratio = ((float) currentHeight / (float) currentWidth);
      
      if (DEBUG)
        log (0, "Resizing Image [size=%dx%d, resizeMode=%s, orientation=%s, ratio(H/W)=%f] to [targetSize=%dx%d]", currentWidth, currentHeight, resizeMode, (ratio <= 1 ? "Landscape/Square" : "Portrait"), ratio, options.width, options.height);
      
      if (resizeMode == ResizeMode.FIT_EXACT) {
        if (DEBUG) log (1, "Resize ResizeMode FIT_EXACT used, no width/height checking or re-calculation will be done.");
      } else if (resizeMode == ResizeMode.BEST_FIT_BOTH) {
        
        float requestedHeightScaling = ((float) options.height / (float) currentHeight);
        float requestedWidthScaling = ((float) options.width / (float) currentWidth);
        float actualScaling = Math.min (requestedHeightScaling, requestedWidthScaling);
        
        options.height = Math.round ((float) currentHeight * actualScaling);
        options.width = Math.round ((float) currentWidth * actualScaling);
        
        if (options.height == currentHeight && options.width == currentWidth) return this;
        
        if (DEBUG) log (1, "Auto-Corrected width and height based on scalingRatio %d.", actualScaling);
        
      } else {
        
        if ((ratio <= 1 && resizeMode == ResizeMode.AUTOMATIC) || resizeMode == ResizeMode.FIT_WIDTH) {
          
          // First make sure we need to do any work in the first place
          if (options.width == src.getWidth ()) return this;
          
          // Save for detailed logging (this is cheap).
          int originalTargetHeight = options.height;
          options.height = (int) Math.ceil ((float) options.width * ratio);
          
          if (DEBUG && originalTargetHeight != options.height)
            log (1, "Auto-Corrected height [from=%d to=%d] to honor image proportions.", originalTargetHeight, options.height);
          
        } else {
          
          // First make sure we need to do any work in the first place
          if (options.height == src.getHeight ()) return this;
          
          // Save for detailed logging (this is cheap).
          int originalTargetWidth = options.width;
          
          options.width = Math.round ((float) options.height / ratio);
          
          if (DEBUG && originalTargetWidth != options.width)
            log (1, "Auto-Corrected width [from=%d to=%d] to honor image proportions.", originalTargetWidth, options.width);
          
        }
        
      }
      
      determineScalingMethod (options.width, options.height, ratio);
      
      if (DEBUG) log (1, "Using Scaling Method: %s", scalingMethod);
      
      Image result = null;
      
      // Now we scale the image
      if (scalingMethod == ScalingMethod.AUTOMATIC) {
        
        scaleImage (options.width, options.height, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        result = new Image (src);
        
      } else if (scalingMethod == ScalingMethod.BALANCED) {
        
        scaleImage (options.width, options.height, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        result = new Image (src);
        
      } else if (scalingMethod == ScalingMethod.QUALITY || scalingMethod == ScalingMethod.ULTRA_QUALITY) {
        
        if (options.width > currentWidth || options.height > currentHeight) {
          
          if (DEBUG) log (1, "QUALITY scale-up, a single BICUBIC scale operation will be used...");
          
          scaleImage (options.width, options.height, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
          result = new Image (src);
          
        } else {
          
          if (DEBUG) log (1, "QUALITY scale-down, incremental scaling will be used...");
          
          scaleImageIncrementally (options.width, options.height, scalingMethod, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
          result = new Image (src);
          
        }
        
      }
      
      if (DEBUG) log (0, "Resized Image in %d ms", System.currentTimeMillis () - t);
      
      // Apply any optional operations (if specified).
      if (result != null) result.apply ();
      
      return this;
      
    }
    
    public Image rotate (Rotation rotation) throws IllegalArgumentException, ImagingOpException {
      
      long t = -1;
      if (DEBUG) t = System.currentTimeMillis ();
      
      if (rotation == null) throw new IllegalArgumentException ("rotation cannot be null");
      
      if (DEBUG) log (0, "Rotating Image [%s]...", rotation);
      
      int newWidth = src.getWidth ();
      int newHeight = src.getHeight ();
      
      AffineTransform tx = new AffineTransform ();
      
      switch (rotation) {
        
        case CW_90:
          newWidth = src.getHeight ();
          newHeight = src.getWidth ();
          
          // Reminder: newWidth == result.getHeight() at this point
          tx.translate (newWidth, 0);
          tx.quadrantRotate (1);
          
          break;
        
        case CW_270:
          newWidth = src.getHeight ();
          newHeight = src.getWidth ();
          
          // Reminder: newHeight == result.getWidth() at this point
          tx.translate (0, newHeight);
          tx.quadrantRotate (3);
          break;
        
        case CW_180:
          tx.translate (newWidth, newHeight);
          tx.quadrantRotate (2);
          break;
        
        case FLIP_HORIZONTAL:
          tx.translate (newWidth, 0);
          tx.scale (-1.0, 1.0);
          break;
        
        case FLIP_VERTICAL:
          tx.translate (0, newHeight);
          tx.scale (1.0, -1.0);
          break;
          
      }
      
      // Create our target image we will render the rotated result to.
      createOptimalImage (newWidth, newHeight);
      
      Image result = new Image (src);
      
      Graphics2D g2d = result.src.createGraphics ();
      
      g2d.drawImage (src, tx, null);
      g2d.dispose ();
      
      if (DEBUG)
        log (0, "Rotation Applied in %d ms, result [width=%d, height=%d]", System.currentTimeMillis () - t, result.src.getWidth (), result.src.getHeight ());
      
      // Apply any optional operations (if specified).
      result.apply ();
      
      return this;
      
    }
    
    protected void log (int depth, String message, Object... params) {
      
      if (DEBUG) {
        
        System.out.print (LOG_PREFIX);
        
        for (int i = 0; i < depth; i++)
          System.out.print ("\t");
        
        System.out.printf (message, params);
        System.out.println ();
        
      }
      
    }
    
    protected BufferedImage createOptimalImage (int width, int height) throws IllegalArgumentException {
      
      if (width <= 0 || height <= 0)
        throw new IllegalArgumentException ("width [" + width + "] and height [" + height + "] must be > 0");
      
      return new BufferedImage (width, height, (src.getTransparency () == Transparency.OPAQUE ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_4BYTE_ABGR));
      
    }
    
    protected BufferedImage copyToOptimalImage () throws IllegalArgumentException {
      
      // Calculate the type depending on the presence of alpha.
      int type = (src.getTransparency () == Transparency.OPAQUE ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_4BYTE_ABGR);
      
      BufferedImage result = new BufferedImage (src.getWidth (), src.getHeight (), type);
      
      // Render the src image into our new optimal source.
      Graphics g = result.getGraphics ();
      
      g.drawImage (src, 0, 0, null);
      g.dispose ();
      
      return result;
      
    }
    
    protected void determineScalingMethod (int targetWidth, int targetHeight, float ratio) {
      
      // Get the primary dimension based on the orientation of the image
      int length = (ratio <= 1 ? targetWidth : targetHeight);
      
      // Figure out which scalingMethod should be used
      if (length <= THRESHOLD_QUALITY_BALANCED) scalingMethod = ScalingMethod.QUALITY;
      else if (length <= THRESHOLD_BALANCED_SPEED) scalingMethod = ScalingMethod.BALANCED;
      
      if (DEBUG) log (2, "AUTOMATIC scaling method selected: %s", scalingMethod.name ());
      
    }
    
    protected BufferedImage scaleImage (int targetWidth, int targetHeight, Object interpolationHintValue) {
      
      // Setup the rendering resources to match the source image's
      BufferedImage result = createOptimalImage (targetWidth, targetHeight);
      
      Graphics2D resultGraphics = result.createGraphics ();
      
      // Scale the image to the new buffer using the specified rendering hint.
      resultGraphics.setRenderingHint (RenderingHints.KEY_INTERPOLATION, interpolationHintValue);
      resultGraphics.drawImage (src, 0, 0, targetWidth, targetHeight, null);
      
      // Just to be clean, explicitly dispose our temporary graphics object
      resultGraphics.dispose ();
      
      // Return the scaled image to the caller.
      return result;
      
    }
    
    protected void scaleImageIncrementally (int targetWidth, int targetHeight, ScalingMethod scalingMethod, Object interpolationHintValue) {
      
      boolean hasReassignedSrc = false;
      
      int incrementCount = 0;
      int currentWidth = src.getWidth ();
      int currentHeight = src.getHeight ();
      
      int fraction = (scalingMethod == ScalingMethod.ULTRA_QUALITY ? 7 : 2);
      
      do {
        
        int prevCurrentWidth = currentWidth;
        int prevCurrentHeight = currentHeight;
        
        if (currentWidth > targetWidth) {
          currentWidth -= (currentWidth / fraction);
          if (currentWidth < targetWidth) currentWidth = targetWidth;
        }
        
        if (currentHeight > targetHeight) {
          
          currentHeight -= (currentHeight / fraction);
          if (currentHeight < targetHeight) currentHeight = targetHeight;
          
        }
        
        if (prevCurrentWidth == currentWidth && prevCurrentHeight == currentHeight) break;
        
        if (DEBUG)
          log (2, "Scaling from [%d x %d] to [%d x %d]", prevCurrentWidth, prevCurrentHeight, currentWidth, currentHeight);
        
        // Render the incremental scaled image.
        BufferedImage incrementalImage = scaleImage (currentWidth, currentHeight, interpolationHintValue);
        
        if (hasReassignedSrc) src.flush ();
        
        src = incrementalImage;
        hasReassignedSrc = true;
        
        // Track how many times we go through this cycle to scale the image.
        incrementCount++;
        
      } while (currentWidth != targetWidth || currentHeight != targetHeight);
      
      if (DEBUG) log (2, "Incrementally Scaled Image in %d steps.", incrementCount);
      
    }
    
    public byte[] toByteArray () {
      return ((DataBufferByte) src.getRaster ().getDataBuffer ()).getData ();
    }
    
    public BufferedInputStream toInputStream () {
      return new BufferedInputStream (new ByteArrayInputStream (toByteArray ()));
    }
    
    public Image copy (OutputStream stream) throws IOException {
      
      ImageIO.write (src, "png", stream);
      return this;
      
    }
    
  }