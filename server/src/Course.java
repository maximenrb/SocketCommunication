package src;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import static java.lang.Float.NaN;

/**
 * Title:        Classes for TP1
 * Copyright:    Copyright (c) 2015
 * Company:      UQAC
 * @author Hamid Mcheick et Hafedh Mili
 * @version 1.0
 * Translate by: Maxime NARBAUD
 */

public class Course {
  private String title;
  private final Hashtable<Student, Float> students;

  public Course() {
    this.students = new Hashtable<>();
  }

  public Course(String courseTitle) {
    this();
    this.title = courseTitle;
  }

  public void setTitle(String unTitre) {
    title = unTitre;
  }

  public String getTitle() {
    return title;
  }

  public void addStudent(Student student) {
    students.put(student, (float) 0);
  }

  public Set<Student> getStudents() {
    return students.keySet();
  }

  public void attributeNote(Student student, float mark) {
    students.put(student, mark);
  }

  public float getNote(Student student) {
    if (students.containsKey(student)) {
      return students.get(student);
    }
    return NaN;
  }

  public String toString() {
    StringBuilder str = new StringBuilder("Course (Title: " + title + " <");

    for (Student student : getStudents()) {
      str.append(student).append(" = ").append(getNote(student)).append(", ");
    }

    str.append(">)");

    return str.toString();
  }
}