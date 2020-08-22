package org.fir3.teye.ui.renderer;

import lombok.Data;
import lombok.Getter;
import org.fir3.teye.util.PowerOfTwo;

import java.nio.ByteBuffer;
import java.util.*;

public abstract class AbstractTextureAtlas<T extends AbstractTexture>
        implements TextureAtlas<T> {
    @Data
    private static class Rectangle {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        public int getSize() {
            return this.width * this.height;
        }
    }

    private enum EdgeType {
        Left,
        Top,
        Right,
        Bottom
    }

    @Data
    private static class Edge {
        private final EdgeType type;
        private final int x;
        private final int y;
        private final int length;
    }

    private static final Comparator<Rectangle> X_COMPARATOR =
            Comparator.comparingInt(Rectangle::getX);

    private static final Comparator<Rectangle> Y_COMPARATOR =
            Comparator.comparingInt(Rectangle::getY);

    // TODO:    Document the "determineLongest" method after a Unit test has
    //          been written and no further result-changing modifications are
    //          required.

    private static Edge determineLongest(
            Rectangle[] rectangles,
            EdgeType type) {
        // TODO:    This is a pretty verbose and unoptimized implementation
        //          of the longest edge algorithm.
        //          Things should be cleaned up.

        int min = Integer.MAX_VALUE, max = 0;

        // Depending on the specified type, we need to sort the passed
        // rectangles array (to ensure that it starts with the smallest x/y
        // and ends with the greatest one)

        switch (type) {
            case Left:
            case Right:
                Arrays.sort(rectangles, AbstractTextureAtlas.Y_COMPARATOR);
                break;

            case Top:
            case Bottom:
                Arrays.sort(rectangles, AbstractTextureAtlas.X_COMPARATOR);
                break;

            default:
                throw new UnsupportedOperationException("Unknown EdgeType!");
        }

        // Determining the min/max x, if we are looking for left or right,
        // otherwise min/max y
        //
        // NOTE: min is inclusive, max is exclusive.

        for (Rectangle rect : rectangles) {
            int rectMin, rectMax;

            switch (type) {
                case Left:
                case Right:
                    rectMin = rect.getX();
                    rectMax = rect.getX() + rect.getWidth();
                    break;

                case Top:
                case Bottom:
                    rectMin = rect.getY();
                    rectMax = rect.getY() + rect.getHeight();
                    break;

                default:
                    throw new UnsupportedOperationException(
                            "Unknown EdgeType!");
            }

            if (rectMin < min)
                min = rectMin;

            if (rectMax > max)
                max = rectMax;
        }

        // Determining the longest edge for each line between min and max

        int[] edgeStarts = new int[max - min];
        int[] edgeLengths = new int[max - min];
        int[] rectStarts = new int[rectangles.length];
        int[] rectEnds = new int[rectangles.length];

        for (int idx = 0; idx < edgeLengths.length; idx++) {
            int pos = min + idx;

            // Determining the length of the current edge for each of the
            // available rectangles

            for (int i = 0; i < rectangles.length; i++) {
                Rectangle rect = rectangles[i];
                int rectStart = -1, rectEnd = -1;

                switch (type) {
                    case Left:
                    case Right:
                        if (rect.getX() <= pos &&
                                (rect.getX() + rect.getWidth()) > pos) {
                            rectStart = rect.getY();
                            rectEnd = rect.getY() + rect.getHeight();
                        }
                        break;

                    case Top:
                    case Bottom:
                        if (rect.getY() <= pos &&
                                (rect.getY() + rect.getHeight()) > pos) {
                            rectStart = rect.getX();
                            rectEnd = rect.getX() + rect.getWidth();
                        }
                        break;

                    default:
                        throw new UnsupportedOperationException(
                                "Unknown EdgeType!");
                }

                rectStarts[i] = rectStart;
                rectEnds[i] = rectEnd;
            }

            // Determining the longest continuous edge by combining the
            // determined edges of rectangles
            //
            // NOTE:    Due to sorting the rectangles array at the
            //          beginning, we can be sure that rectangles are in
            //          the right order.

            int longestEdgeLength = -1, longestEdgeStart = -1;
            int currentEdgeLength = -1, currentEdgeStart = -1;

            for (int i = 0; i < rectangles.length; i++) {
                int subEdgeStart = rectStarts[i];
                int subEdgeEnd = rectEnds[i];

                if (subEdgeStart == -1 || subEdgeEnd == -1)
                    continue;

                if (currentEdgeStart == -1 ||
                        (currentEdgeStart + currentEdgeLength)
                                != subEdgeStart) {
                    if (currentEdgeLength > longestEdgeLength) {
                        longestEdgeStart = currentEdgeStart;
                        longestEdgeLength = currentEdgeLength;
                    }

                    currentEdgeStart = subEdgeStart;
                    currentEdgeLength = subEdgeEnd - subEdgeStart;
                    continue;
                }

                currentEdgeLength += subEdgeEnd - subEdgeStart;
            }

            if (currentEdgeLength > longestEdgeLength) {
                longestEdgeStart = currentEdgeStart;
                longestEdgeLength = currentEdgeLength;
            }

            edgeStarts[idx] = longestEdgeStart;
            edgeLengths[idx] = longestEdgeLength;
        }

        // Determining the indices of the two longest edges

        int longestEdgeIdx = -1, longestEdgeLength = 0,
                secondLongestEdgeIdx = -1, secondLongestEdgeLength = 0;

        boolean preferSmaller = type == EdgeType.Left || type == EdgeType.Top;

        for (int idx = 0; idx < edgeLengths.length; idx++) {
            int length = edgeLengths[idx];

            if (length < secondLongestEdgeLength)
                continue;

            if (length < longestEdgeLength ||
                    (length == longestEdgeLength && preferSmaller)) {
                if (length == secondLongestEdgeLength && preferSmaller)
                    continue;

                secondLongestEdgeIdx = idx;
                secondLongestEdgeLength = length;
                continue;
            }

            if (longestEdgeLength > secondLongestEdgeLength ||
                    (preferSmaller &&
                            longestEdgeIdx < secondLongestEdgeIdx) ||
                    (!preferSmaller &&
                            longestEdgeIdx > secondLongestEdgeIdx)) {
                secondLongestEdgeIdx = longestEdgeIdx;
                secondLongestEdgeLength = longestEdgeLength;
            }

            longestEdgeIdx = idx;
            longestEdgeLength = length;
        }

        // Depending on the requested edge type, we determine whether we
        // shall return the longest or the second longest edge index.

        int idx;

        switch (type) {
            case Left:
                idx = longestEdgeIdx;

                if (longestEdgeIdx > secondLongestEdgeIdx)
                    idx = secondLongestEdgeIdx;

                return new Edge(
                        type,
                        min + idx, edgeStarts[idx],
                        edgeLengths[idx]);

            case Top:
                idx = longestEdgeIdx;

                if (longestEdgeIdx > secondLongestEdgeIdx)
                    idx = secondLongestEdgeIdx;

                return new Edge(
                        type,
                        edgeStarts[idx], min + idx,
                        edgeLengths[idx]);

            case Right:
                idx = longestEdgeIdx;

                if (longestEdgeIdx < secondLongestEdgeIdx)
                    idx = secondLongestEdgeIdx;

                return new Edge(
                        type,
                        min + idx, edgeStarts[idx],
                        edgeLengths[idx]);

            case Bottom:
                idx = longestEdgeIdx;

                if (longestEdgeIdx < secondLongestEdgeIdx)
                    idx = secondLongestEdgeIdx;

                return new Edge(
                        type,
                        edgeStarts[idx], min + idx,
                        edgeLengths[idx]);

            default:
                throw new UnsupportedOperationException("Unknown EdgeType!");
        }
    }

    /**
     * Computes the border between the two specified rectangles.
     *
     * NOTE:    Only the width and the height of the rectangles are taken into
     *          account, while the x- and y-coordinates are being ignored.
     *
     * @param a The first rectangle.
     * @param b The second rectangle.
     *
     * @return  The size of the border. This is always a positive value.
     */
    private static int computeBorder(Rectangle a, Rectangle b) {
        return Math.abs(a.getSize() - b.getSize());
    }

    /**
     * Returns whether the specified <code>inner</code> {@link Rectangle} fits
     * into the specified <code>outer</code> {@link Rectangle}.
     *
     * NOTE:    Only the width and the height of the rectangles are taken into
     *          account, while the x- and y-coordinates are being ignored.
     *
     * @param outer The outer rectangle.
     * @param inner The inner rectangle.
     *
     * @return  Either <code>true</code>, if the <code>inner</code>
     *          {@link Rectangle} fits into the <code>outer</code>
     *          {@link Rectangle}, otherwise <code>false</code>.
     */
    private static boolean fits(Rectangle outer, Rectangle inner) {
        return (outer.getWidth() >= inner.getWidth())
                && (outer.getHeight() >= inner.getHeight());
    }

    /**
     * Determines the greatest value of all passed <code>values</code>.
     *
     * @param values    An array whose greatest element shall be determined.
     * @return  The greatest element inside the specified <code>values</code>
     *          array.
     *
     * @throws IllegalArgumentException If the specified <code>values</code>
     *                                  array does not contain any element.
     */
    private static int max(int... values) {
        if (values.length == 0)
            throw new IllegalArgumentException("No element in array!");

        int result = values[0];

        for (int i = 1; i < values.length; i++)
            result = Math.max(result, values[i]);

        return result;
    }

    /**
     * Determines the smallest value of all passed <code>values</code>.
     *
     * @param values    An array whose smallest element shall be determined.
     * @return  The smallest element inside the specified <code>values</code>
     *          array.
     *
     * @throws IllegalArgumentException If the specified <code>values</code>
     *                                  array does not contain any element.
     */
    private static int min(int... values) {
        if (values.length == 0)
            throw new IllegalArgumentException("No element in array!");

        int result = values[0];

        for (int i = 1; i < values.length; i++)
            result = Math.min(result, values[i]);

        return result;
    }

    /**
     * If the <code>original</code> and the <code>mask</code>
     * {@link Rectangle}s are overlapping, this method will resize the
     * <code>original</code> {@link Rectangle} to stop overlapping.
     *
     * @param mask      The rectangle that may overlap with the
     *                  <code>original</code> one and will not become resized.
     *
     * @param original  The rectangle that may overlap with he
     *                  <code>mask</code> one and will become resized, if it is
     *                  so.
     *
     * @return  The adjusted version of the <code>original</code> rectangle.
     */
    private static Rectangle adjust(
            Rectangle mask,
            Rectangle original) {
        int x = original.getX();
        int y = original.getY();

        if (mask.getX() < x)
            x = mask.getX() + mask.getWidth();

        if (mask.getY() < y)
            y = mask.getY() + mask.getHeight();

        int width = original.getWidth() - (x - original.getX());
        int height = original.getHeight() - (x - original.getX());

        if (mask.getX() > x)
            width = Math.min(width, (mask.getX() - x));

        if (mask.getY() > y)
            height = Math.min(height, (mask.getY() - y));

        if (width < 1 || height < 1)
            return null;

        return new Rectangle(x, y, width, height);
    }

    @Getter
    private final int width, height;
    private final Set<T> fragments;
    private final Set<Rectangle> unoccupiedRectangles;

    /**
     * Creates a new instance of this class.
     *
     * @param width     The total width of the atlas.
     * @param height    The total height of the atlas.
     *
     * @throws IllegalArgumentException If <code>width</code> or
     *                                  <code>height</code> are less than one
     *                                  or no power of two.
     */
    protected AbstractTextureAtlas(int width, int height) {
        if (width < 1 || !PowerOfTwo.isPowerOfTwo(width))
            throw new IllegalArgumentException("Invalid width!");

        if (height < 1 || !PowerOfTwo.isPowerOfTwo(height))
            throw new IllegalArgumentException("Invalid height!");

        this.width = width;
        this.height = height;
        this.fragments = Collections.newSetFromMap(new IdentityHashMap<>());
        this.unoccupiedRectangles = new HashSet<>();
        this.unoccupiedRectangles.add(new Rectangle(0, 0, width, height));
    }

    @Override
    public boolean contains(T fragment) {
        return this.fragments.contains(fragment);
    }

    @Override
    public int getX(T fragment) {
        if (!this.fragments.contains(fragment))
            return -1;

        return fragment.getX();
    }

    @Override
    public int getY(T fragment) {
        if (!this.fragments.contains(fragment))
            return -1;

        return fragment.getY();
    }

    @Override
    public boolean insert(T fragment) {
        if (this.fragments.contains(fragment))
            return true;

        // Check, if the specified fragent is not part of another atlas already

        if (fragment.getData() == null
                || fragment.getX() != -1
                || fragment.getY() != -1)
            throw new IllegalArgumentException(
                    "fragment already part of another atlas!");

        // Finding the best unoccupied location

        Rectangle designatedLocation = this.findRectangle(
                fragment.getWidth(),
                fragment.getHeight());

        if (designatedLocation == null)
            return false;

        // Clipping the designatedLocation rectangle to the fragment's sizes
        // and adjusting the unoccupiedRectangles set

        this.unoccupiedRectangles.remove(designatedLocation);

        boolean greaterWidth =
                designatedLocation.getWidth() > fragment.getWidth();

        boolean greaterHeight =
                designatedLocation.getHeight() > fragment.getHeight();

        if (greaterWidth) {
            this.freeRectangle(new Rectangle(
                    designatedLocation.getX() + fragment.getWidth(),
                    designatedLocation.getY(),
                    designatedLocation.getWidth() - fragment.getWidth(),
                    fragment.getHeight()));
        }

        if (greaterHeight) {
            this.freeRectangle(new Rectangle(
                    designatedLocation.getX(),
                    designatedLocation.getY() + fragment.getHeight(),
                    fragment.getWidth(),
                    designatedLocation.getHeight() - fragment.getHeight()));
        }

        if (greaterWidth && greaterHeight) {
            this.freeRectangle(new Rectangle(
                    designatedLocation.getX() + fragment.getWidth(),
                    designatedLocation.getY() + fragment.getHeight(),
                    designatedLocation.getWidth() - fragment.getWidth(),
                    designatedLocation.getHeight() - fragment.getHeight()));
        }

        // Moving the texture data and adjusting the fragment information

        this.insert(
                designatedLocation.getX(), designatedLocation.getY(),
                fragment.getWidth(), fragment.getHeight(),
                fragment.getData(),
                fragment.getPixelFormat());

        fragment.setX(designatedLocation.getX());
        fragment.setY(designatedLocation.getY());
        fragment.setData(null);

        this.fragments.add(fragment);
        return true;
    }

    @Override
    public void free(T fragment) {
        if (!this.fragments.contains(fragment))
            return;

        // Consistency check

        if (fragment.getData() != null &&
                (fragment.getX() != -1 || fragment.getY() != -1))
            throw new IllegalArgumentException("fragment is inconsistent!");

        // First of all, we need to restore the fragment's texture data

        ByteBuffer buffer = ByteBuffer.allocate(
                fragment.getWidth()
                        * fragment.getHeight()
                        * fragment.getPixelFormat().getBytesPerPixel());

        this.copy(
                fragment.getX(), fragment.getY(),
                fragment.getWidth(), fragment.getHeight(),
                fragment.getPixelFormat(),
                buffer);

        buffer.flip();

        // Removing the fragment from this set and freeing its occupied
        // rectangle.

        this.fragments.remove(fragment);
        this.freeRectangle(new Rectangle(
                fragment.getX(), fragment.getY(),
                fragment.getWidth(), fragment.getHeight()));

        // Adjusting the fragment itself

        fragment.setX(-1);
        fragment.setY(-1);
        fragment.setData(buffer);
    }

    @Override
    public float getOccupation() {
        // Determining the biggest rectangle of unoccupied space

        int biggestSize = 0;

        for (Rectangle unoccupiedRectangle : this.unoccupiedRectangles) {
            int size = unoccupiedRectangle.getSize();

            if (biggestSize > size)
                continue;

            biggestSize = size;
        }

        return 1.0F - ((float) biggestSize /
                (float) (this.width * this.height));
    }

    @Override
    public float computeBorderRatio(int width, int height) {
        Rectangle innerRectangle = new Rectangle(0, 0, width, height);
        Rectangle designatedRectangle = this.findRectangle(width, height);

        if (designatedRectangle == null)
            return -1.0F;

        int border = AbstractTextureAtlas.computeBorder(
                designatedRectangle,
                innerRectangle);

        return (float) border / (float) innerRectangle.getSize();
    }

    /**
     * Returns the smallest {@link Rectangle} whose width and height are
     * greater or equal to the specified <code>width</code> and
     * <code>height</code>.
     *
     * @param width     The width of the requested rectangle.
     * @param height    The height of the requested rectangle.
     *
     * @return  Either a matching {@link Rectangle} of this atlas instance that
     *          is unoccupied or <code>null</code>, if there is no matching
     *          one.
     */
    private Rectangle findRectangle(int width, int height) {
        // TODO:    If required, this method should attempt to rearrange the
        //          rectangles to provide the required rectangle.

        Rectangle inner = new Rectangle(0, 0, width, height);
        Rectangle best = null;
        int bestBorder = Integer.MAX_VALUE;

        for (Rectangle potentiallyBest : this.unoccupiedRectangles) {
            // Ignoring any rectangle that is too small.

            if (!AbstractTextureAtlas.fits(potentiallyBest, inner))
                continue;

            // If the potentiallyBest's border size is less than the bestBorder
            // value, we choose the potentiallyBest rectangle as new best
            // rectangle.

            int border = AbstractTextureAtlas.computeBorder(
                    potentiallyBest,
                    inner);

            if (bestBorder > border) {
                best = potentiallyBest;
                bestBorder = border;
            }

            // If bestBorder is zero, we can stop looking for a better match.

            if (bestBorder == 0)
                break;
        }

        return best;
    }

    /**
     * Adds the specified <code>rectangle</code> to the
     * {@link #unoccupiedRectangles} set and attempts to merge it with
     * neighbours.
     *
     * @param rectangle The rectangle of this atlas that shall be freed.
     */
    private void freeRectangle(Rectangle rectangle) {
        // Validating that the passed rectangle is inside this atlas

        int rectX = rectangle.getX();
        int rectY = rectangle.getY();
        int rectWidth = rectangle.getWidth();
        int rectHeight = rectangle.getHeight();
        int rectEndX = rectX + rectWidth;
        int rectEndY = rectY + rectHeight;

        if (rectX < 0 || rectY < 0 ||
                rectEndX > this.width || rectEndY > this.height)
            throw new IllegalArgumentException("rectangle out of bounds!");

        // Collecting all unoccupied rectangles that adjoin the passed
        // rectangle (same edge is mandatory; corner point neighbours do not
        // matter)

        Set<Rectangle> neighbours = new HashSet<>();

        for (Rectangle potentialNeighbour : this.unoccupiedRectangles) {
            int pnRectX = potentialNeighbour.getX();
            int pnRectY = potentialNeighbour.getY();
            int pnRectWidth = potentialNeighbour.getWidth();
            int pnRectHeight = potentialNeighbour.getHeight();
            int pnRectEndX = pnRectX + pnRectWidth;
            int pnRectEndY = pnRectY + pnRectHeight;

            // Left

            if (pnRectEndX == rectX &&
                    (pnRectY >= rectY && pnRectY < rectEndY)) {
                neighbours.add(potentialNeighbour);
                continue;
            }

            // Top

            if (pnRectEndY == rectY &&
                    (pnRectX >= rectX && pnRectX < rectEndX)) {
                neighbours.add(potentialNeighbour);
                continue;
            }

            // Right

            if (rectEndX == pnRectX &&
                    (pnRectY >= rectY && pnRectY < rectEndY)) {
                neighbours.add(potentialNeighbour);
                continue;
            }

            // Left

            if (rectEndY == pnRectY &&
                    (pnRectX >= rectX && pnRectX < rectEndX))
                neighbours.add(potentialNeighbour);
        }

        // Now, we attempt to optimize the rectangle and its unoccupied
        // neighbours. This means that we are looking for the biggest rectangle
        // that fits in the passed rectangle and all of its direct neighbours.
        //
        // For this, we need to determine the largest continuous edge that is
        // unoccupied at the top, the bottom, the left and the right.

        Rectangle[] rectangles = new Rectangle[neighbours.size() + 1];
        rectangles[0] = rectangle;
        System.arraycopy(
                neighbours.toArray(new Rectangle[0]), 0,
                rectangles, 1, neighbours.size());

        Edge left = AbstractTextureAtlas.determineLongest(
                rectangles, EdgeType.Left);

        Edge right = AbstractTextureAtlas.determineLongest(
                rectangles, EdgeType.Right);

        Edge top = AbstractTextureAtlas.determineLongest(
                rectangles, EdgeType.Top);

        Edge bottom = AbstractTextureAtlas.determineLongest(
                rectangles, EdgeType.Bottom);

        // Constructing the optimized rectangle

        int oRectX = AbstractTextureAtlas.max(
                top.getX(), left.getX(), bottom.getX());

        int oRectY = AbstractTextureAtlas.max(
                left.getY(), top.getY(), right.getY());

        int oRectWidth = AbstractTextureAtlas.min(
                (top.getX() + top.getLength()) - oRectX,
                (right.getX() + 1) - oRectX,
                (bottom.getX() + bottom.getLength()) - oRectX);

        int oRectHeight = AbstractTextureAtlas.min(
                (left.getY() + left.getLength()) - oRectY,
                (bottom.getY() + 1) - oRectY,
                (right.getY() + right.getLength()) - oRectY);

        // If the width or the height of the optimized rectangle is zero or
        // bellow, we perform no optimization and just add the original
        // rectangle to the unoccupied set

        if (oRectWidth < 1 || oRectHeight < 1) {
            this.unoccupiedRectangles.add(rectangle);
            return;
        }

        Rectangle optimizedRect = new Rectangle(
                oRectX, oRectY,
                oRectWidth, oRectHeight);

        this.unoccupiedRectangles.removeAll(neighbours);
        this.unoccupiedRectangles.add(optimizedRect);

        // Adjusting the neighbours and the original rectangle

        for (Rectangle affectedRect : rectangles) {
            affectedRect = AbstractTextureAtlas.adjust(
                    optimizedRect, affectedRect);

            if (affectedRect == null)
                continue;

            this.unoccupiedRectangles.add(affectedRect);
        }
    }

    /**
     * Inserts the specified texture <code>data</code> with the specified
     * <code>pixelFormat</code> at the specified location.
     *
     * @param x             The x-coordinate of the texture's location inside
     *                      this atlas where the texture shall be inserted.
     *
     * @param y             The y-coordinate of the texture's location inside
     *                      this atlas where the texture shall be inserted.
     *
     * @param width         The width of the texture that shall be inserted.
     * @param height        The height of the texture that shall be inserted.
     * @param data          The pixel data of the texture that shall be
     *                      inserted.
     *
     * @param pixelFormat   The format of the texture's pixel data.
     *
     * @throws IllegalArgumentException If the specified
     *                                  <code>pixelFormat</code> is not
     *                                  supported.
     */
    protected abstract void insert(
            int x, int y,
            int width, int height,
            ByteBuffer data,
            ColorModel pixelFormat);

    /**
     * Copies the delimited fragment into the specified <code>dst</code>
     * {@link ByteBuffer}.
     *
     * NOTE:    The implementation is not allowed to call
     *          {@link ByteBuffer#flip()} on the specified <code>dst</code>
     *          {@link ByteBuffer} instance.
     *
     * @param x                 The x-coordinate of the fragment.
     * @param y                 The y-coordinate of the fragment.
     * @param width             The width of the fragment.
     * @param height            The height of the fragment.
     * @param dstPixelFormat    The destination format of the fragment's pixel
     *                          data.
     *
     * @param dst               The destination of the copied pixel data.
     *
     * @throws IllegalArgumentException If the specified
     *                                  <code>pixelFormat</code> is not
     *                                  supported.
     */
    protected abstract void copy(
            int x, int y,
            int width, int height,
            ColorModel dstPixelFormat,
            ByteBuffer dst);
}
