package models;

public class Huella {
    private int fid;
    private int size;
    private int valid;
    private String template;

    public Huella(int fid, int size, int valid, String template) {
        this.fid = fid;
        this.size = size;
        this.valid = valid;
        this.template = template;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getValid() {
        return valid;
    }

    public void setValid(int valid) {
        this.valid = valid;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}