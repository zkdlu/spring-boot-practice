package com.zkdlu.rest.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class HelloController {
    @GetMapping(value = "/helloworld/string")
    public @ResponseBody String helloworldString() {
        return "helloworld";
    }

    @GetMapping(value = "/helloworld/json")
    public @ResponseBody Hello helloworldJson() {
        Hello hello = new Hello();
        hello.setMessage("helloworld");
        return hello;
    }

    @GetMapping(value = "/helloworld/page")
    public String helloworld(Map model) {
        model.put("message", "hello world");

        return "helloworld";
    }

    @Setter
    @Getter
    public class Hello {
        private String message;
    }
}