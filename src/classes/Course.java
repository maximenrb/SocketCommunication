package classes;

import java.util.Hashtable;
import java.util.Set;
import static java.lang.Float.NaN;

/**
 * Title:        Course class for TP1
 * Copyright:    Copyright (c) 2015
 * Company:      UQAC
 * Translate by: Maxime NARBAUD
 * @author Hamid Mcheick et Hafedh Mili
 * @version 1.1
 */

public class Course {
  private String title;
  private final Hashtable<classes.Student, Float> students;

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

  public void addStudent(classes.Student student) {
    students.put(student, (float) 0);
  }

  public Set<classes.Student> getStudents() {
    return students.keySet();
  }

  public void attributeNote(classes.Student student, float mark) {
    students.put(student, mark);
  }

  public float getNote(classes.Student student) {
    if (students.containsKey(student)) {
      return students.get(student);
    }
    return NaN;
  }

  public String toString() {
    StringBuilder str = new StringBuilder("Course (Title: " + title + " <");

    for (classes.Student student : getStudents()) {
      str.append(student).append(" = ").append(getNote(student)).append(", ");
    }

    str.append(">)");

    return str.toString();
  }
}