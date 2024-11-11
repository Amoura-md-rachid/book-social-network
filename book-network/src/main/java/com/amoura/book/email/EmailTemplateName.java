package com.amoura.book.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {

    AVTICATE_ACOUNT("activate_account")

    ;
    private final String name;
    EmailTemplateName(String name) {
        this.name = name;
    }


}
