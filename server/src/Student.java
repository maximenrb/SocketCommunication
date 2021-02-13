package src;

import java.util.Vector;

/**
 * Title:        Classes for TP1
 * Description:
 * Copyright:    Copyright (c) 2015
 * Company:      UQAC
 * @author Hamid Mcheick et Hafedh Mili
 * @version 1.0
 */

public class Student {
  private String name;
  private final Vector<Course> courses;

  public Student() {
    this.courses = new Vector<>();
  }

  public Student(String name) {
    this();
    this.name = name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void courseRegistration(Course course) {
    courses.add(course);

    course.addStudent(this);
  }

  public float getMean() {
    float markTotal = 0;
    int coursesNumber = 0;

    for (Course course : courses) {
      markTotal += course.getNote(this);
      coursesNumber++;
    }

    return markTotal/(float)coursesNumber;
  }

  public String toString() {
    return name + "[" + getMean() + "]";
  }
}