package com.reactnativemediapicker.models;

public class Response {
  private String path;
  private int size;
  private int width;
  private int height;

  public Response(String path, int size, int width, int height) {
    this.path = path;
    this.size = size;
    this.width = width;
    this.height = height;
  }

  public String getPath() {
    return "file://" + path;
  }

  public int getSize() {
    return size;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}
