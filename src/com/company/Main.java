package com.company;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Person {
    private String name, surname;
    //  private Integer id = 0; //  ClassCastExc --- any other classes
                                //  but String do not participate in String hierarchy
    public Person(){}

    public Person(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return name.equals(person.name) && surname.equals(person.surname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname);
    }
}
public class Main {
    public static void main(String[] args) throws Exception{
        Person person = new Person("Alex", "Kutnii");

        String json = getJson(person);

        Person person1 = getFromJson(json, Person.class);

        json = getJson(person1);

        System.out.println(json);

        System.out.println();

        System.out.println("Objects are equal: " + person.equals(person1));

    }

    private static <T> T getFromJson(String json, Class<T> clazz) throws InstantiationException, IllegalAccessException {
        T obj = clazz.newInstance();

        Map<String,Object> map = getJsonMap(json);

        Arrays.stream(clazz.getDeclaredFields())
                .peek(f -> f.setAccessible(true))
                .forEachOrdered(f -> {
                    try {
                        f.set(obj,  f.getType().cast(map.get(f.getName())));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });

        return (T)obj;
    }

    public static  String getJson(Object obj){
        Stream<Field> fields = Arrays.stream(obj.getClass().getDeclaredFields());

        Map<String, Object> map = getJsonMap(fields, obj);

        // can be string builder, but it's not recommended by ide
        String sb = obj.getClass().getSimpleName() + ":\n" +
                "{\n" +
                map.entrySet().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",\n")) +
                "\n}";

        return sb;
    }

    //  here should be a well minded algorithm but... no XD
    //  can be broken with malicious values
    private static Map<String, Object> getJsonMap(String json){
        return Arrays.stream(json.split("[:,{}\\s]"))
                .filter(s -> s.matches("\\w*=\\w*"))
                .collect(Collectors.toMap(
                        s -> s.split("=")[0],
                        s -> s.split("=")[1]
                ));
    }
    private static Map<String, Object> getJsonMap(Stream<Field> fields, Object obj){
        Map<String, Object> map = fields
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.
                        toMap(Field::getName,
                                field -> {
                                    try {
                                        return field.get(obj);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                    return null;//  don't know what it should be XD
                                }));
        return map;
    }

}

