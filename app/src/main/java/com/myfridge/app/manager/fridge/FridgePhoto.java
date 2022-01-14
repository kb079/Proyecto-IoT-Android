package com.myfridge.app.manager.fridge;

public class FridgePhoto {

    private long tiempo;
    private String titulo;
    private String url;


    public FridgePhoto(long tiempo, String titulo, String url) {
        this.tiempo = tiempo;
        this.titulo = titulo;
        this.url = url;
    }

    public FridgePhoto(){}

    public long getTiempo() {
        return tiempo;
    }

    public void setTiempo(long tiempo) {
        this.tiempo = tiempo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
