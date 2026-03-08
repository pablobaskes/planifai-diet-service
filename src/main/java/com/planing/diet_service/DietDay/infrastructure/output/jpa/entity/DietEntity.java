package com.planing.diet_service.DietDay.infrastructure.output.jpa.entity;

import com.planing.diet_service.DietEntity.infrastructure.output.jpa.entity.DietDayEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diets")
@Getter
@Setter
@NoArgsConstructor
public class DietEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private Integer caloriesTarget;

    private LocalDate initDate;

    private LocalDate endDate;

    @OneToMany(mappedBy = "diet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DietDayEntity> days = new ArrayList<>();
}
