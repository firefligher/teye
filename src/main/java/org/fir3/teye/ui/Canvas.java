package org.fir3.teye.ui;

public class Canvas {
    public void fillRectangle(int x, int y, int width, int height) {
        // TODO
    }

    public void drawImage(
            int x, int y,
            int width, int height,
            Image image,
            int imgX, int imgY,
            int imgWidth, int imgHeight) {
        // TODO
    }

    public void drawImage(int x, int y, Image image) {
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        this.drawImage(
                x, y,
                imgWidth, imgHeight,
                image,
                0, 0,
                imgWidth, imgHeight);
    }

    public void drawImage(int x, int y, Image image, int imgX, int imgY) {
        int width = image.getWidth() - imgX;
        int height = image.getHeight() - imgY;

        this.drawImage(x, y, width, height, image, imgX, imgY, width, height);
    }

    public void drawImage(int x, int y, int width, int height, Image image) {
        this.drawImage(
                x, y,
                width, height,
                image,
                0, 0,
                width, height);
    }

    public void drawImage(
            int x, int y,
            int width, int height,
            Image image,
            int imgX, int imgY) {
        int imgWidth = image.getWidth() - imgX;
        int imgHeight = image.getHeight() - imgY;

        this.drawImage(
                x, y,
                width, height,
                image,
                imgX, imgY,
                imgWidth, imgHeight);
    }

    public void setColor(Color color) {
        // TODO
    }

    public Color getColor() {
        // TODO
        return null;
    }
}
