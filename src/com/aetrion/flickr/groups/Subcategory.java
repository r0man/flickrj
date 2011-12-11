/*
 * Copyright (c) 2005 Aetrion LLC.
 */
package com.aetrion.flickr.groups;

/**
 * Class representing a subcategory.
 *
 * @author Anthony Eden
 */
public class Subcategory {
	private static final long serialVersionUID = 12L;

    private int id;
    private String name;
    private int count;

    public Subcategory() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
