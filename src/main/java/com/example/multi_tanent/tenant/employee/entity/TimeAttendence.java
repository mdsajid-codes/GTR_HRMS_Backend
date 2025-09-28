package com.example.multi_tanent.tenant.employee.entity;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "time_attendence")
@Setter
@Getter
@ToString(exclude = "employee")
@EqualsAndHashCode(exclude = "employee")
@NoArgsConstructor
@AllArgsConstructor
public class TimeAttendence {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @JsonBackReference
    private Employee employee;

    private String timeType;
    private String workType;
    private String shiftType;
    private String weeklyOffPolicy;
    private String leaveGroup;
    private String attendenceCaptureScheme;
    private String holidayList;
    private String expensePolicy;
    private String attendenceTrackingPolicy;
    private String recruitmentPolicy;
    private Boolean isRosterBasedEmployee;
}
