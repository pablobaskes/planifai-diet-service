package com.planing.diet_service.Diet.infrastructure.output.jpa.entity;

import com.planing.diet_service.DietDay.infrastructure.output.jpa.entity.DietEntity;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.entity.MealSlotEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diet_days")
@Getter
@Setter
@NoArgsConstructor
public class DietDayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_id")
    private DietEntity diet;

    @OneToMany(mappedBy = "dietDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealSlotEntity> mealSlots = new ArrayList<>();
}
