package com.jenfer.frentmatch.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 713255685775607121L;

    protected int pageSize = 10;

    protected int pageNum = 1;



}
