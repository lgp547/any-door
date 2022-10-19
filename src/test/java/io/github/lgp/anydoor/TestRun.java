package io.github.lgp.anydoor;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestRun {

    public static final String content = "{\n" +
            "  \"user\": {\n" +
            "    \"name\": \"lgpUser\",\n" +
            "    \"id\": 2\n" +
            "  },\n" +
            "  \"name\": \"lgp\",\n" +
            "  \"id\": 1,\n" +
            "  \"strings\": [\n" +
            "    \"str1\",\n" +
            "    \"str2\"\n" +
            "  ],\n" +
            "  \"users\": [\n" +
            "    {\n" +
            "      \"user\": {\n" +
            "        \"name\": \"lgpUser\",\n" +
            "        \"id\": 4\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"user\": {\n" +
            "        \"name\": \"lgpUser\",\n" +
            "        \"id\": 3\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";


    @RequestMapping("/testLgp")
    public void testLgp() {
        System.out.println("testLgp");
    }

    @RequestMapping("/testSingle")
    public void testSingle(String name) {
        System.out.println("testSingle " + name);
    }

    @RequestMapping("/testSingle1")
    public void testSingle1(@RequestBody User user) {
        System.out.println("testSingle1 " + user);
    }

    @RequestMapping("/testSingle2")
    public void testSingle2(List<String> strings) {
        System.out.println("testSingle2 " + strings);
    }

    @RequestMapping("/testSingle3")
    public void testSingle3(List<User> users) {
        System.out.println("testSingle3 " + users);
    }

    @RequestMapping("/testMultiple")
    public void testMultiple(String name, Integer id) {
        System.out.println("testMultiple " + name);
        System.out.println("testMultiple " + id);
    }

    @RequestMapping("/testMultiple2")
    public void testMultiple2(Integer id, String name) {
        System.out.println("testMultiple2 " + id);
        System.out.println("testMultiple2 " + name);
    }
}
