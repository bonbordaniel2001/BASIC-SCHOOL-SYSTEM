package com.example.util

import com.example.data.model.StudentGrade

/**
 * Utility for Grade Point Average (GPA) calculations and trend analytics.
 * Standard 4.0 GPA scale mapped to WASSCE/BECE grading criteria.
 */
object GpaCalculator {

    /**
     * Maps percentage total score (0-100) to a 4.0 GPA scale.
     */
    fun scoreToGpa(score: Double): Double {
        return when {
            score >= 80.0 -> 4.00
            score >= 75.0 -> 3.50
            score >= 70.0 -> 3.00
            score >= 65.0 -> 2.50
            score >= 60.0 -> 2.00
            score >= 55.0 -> 1.50
            score >= 50.0 -> 1.00
            score >= 45.0 -> 0.50
            else -> 0.00
        }
    }

    /**
     * Maps percentage total score (0-100) to official letter grade.
     */
    fun scoreToLetter(score: Double): String {
        return when {
            score >= 80.0 -> "A1"
            score >= 75.0 -> "B2"
            score >= 70.0 -> "B3"
            score >= 65.0 -> "C4"
            score >= 60.0 -> "C5"
            score >= 55.0 -> "C6"
            score >= 50.0 -> "D7"
            score >= 45.0 -> "E8"
            else -> "F9"
        }
    }

    /**
     * Description associated with a letter grade.
     */
    fun letterToDescription(letter: String): String {
        return when (letter.uppercase().take(2)) {
            "A1" -> "Excellent"
            "B2" -> "Very Good"
            "B3" -> "Good"
            "C4", "C5", "C6" -> "Credit"
            "D7", "E8" -> "Pass"
            "F9" -> "Fail"
            else -> "Unclassified"
        }
    }

    /**
     * Calculates average GPA for a list of student grade entries.
     */
    fun calculateGpaForGrades(grades: List<StudentGrade>): Double {
        if (grades.isEmpty()) return 0.0
        val sumGpa = grades.sumOf { scoreToGpa(it.totalScore) }
        return sumGpa / grades.size
    }

    enum class GpaTrendDirection {
        IMPROVING, // Trend going up (+0.05 GPA or higher)
        STEADY,    // Constant (+/- 0.05 GPA)
        DECLINING, // Trend going down (-0.05 GPA or lower)
        NEW_ENTRY  // Single term data
    }

    data class StudentGpaTrend(
        val studentId: Long,
        val studentName: String,
        val className: String,
        val term1Gpa: Double?,
        val term2Gpa: Double?,
        val term3Gpa: Double?,
        val cumulativeGpa: Double,
        val gpaDeltaRecent: Double?, // Delta between latest term and prior term
        val trendDirection: GpaTrendDirection,
        val topLetterClassification: String,
        val totalSubjectsCount: Int
    )

    /**
     * Computes multi-term GPA trend breakdown for a given student.
     */
    fun computeStudentGpaTrend(
        studentId: Long,
        studentName: String,
        className: String,
        allGradesForStudent: List<StudentGrade>
    ): StudentGpaTrend {
        val t1Grades = allGradesForStudent.filter { it.academicTerm.contains("Term 1", ignoreCase = true) }
        val t2Grades = allGradesForStudent.filter { it.academicTerm.contains("Term 2", ignoreCase = true) }
        val t3Grades = allGradesForStudent.filter { it.academicTerm.contains("Term 3", ignoreCase = true) }

        val t1Gpa = if (t1Grades.isNotEmpty()) calculateGpaForGrades(t1Grades) else null
        val t2Gpa = if (t2Grades.isNotEmpty()) calculateGpaForGrades(t2Grades) else null
        val t3Gpa = if (t3Grades.isNotEmpty()) calculateGpaForGrades(t3Grades) else null

        val cumulativeGpa = calculateGpaForGrades(allGradesForStudent)

        // Compare recent terms
        val (priorGpa, latestGpa) = when {
            t3Gpa != null && t2Gpa != null -> Pair(t2Gpa, t3Gpa)
            t2Gpa != null && t1Gpa != null -> Pair(t1Gpa, t2Gpa)
            else -> Pair(null, t3Gpa ?: t2Gpa ?: t1Gpa)
        }

        val delta = if (priorGpa != null && latestGpa != null) latestGpa - priorGpa else null
        val trendDir = when {
            delta == null -> GpaTrendDirection.NEW_ENTRY
            delta >= 0.05 -> GpaTrendDirection.IMPROVING
            delta <= -0.05 -> GpaTrendDirection.DECLINING
            else -> GpaTrendDirection.STEADY
        }

        val overallAvgScore = if (allGradesForStudent.isNotEmpty()) allGradesForStudent.map { it.totalScore }.average() else 0.0
        val topLetter = scoreToLetter(overallAvgScore)

        return StudentGpaTrend(
            studentId = studentId,
            studentName = studentName,
            className = className,
            term1Gpa = t1Gpa,
            term2Gpa = t2Gpa,
            term3Gpa = t3Gpa,
            cumulativeGpa = cumulativeGpa,
            gpaDeltaRecent = delta,
            trendDirection = trendDir,
            topLetterClassification = topLetter,
            totalSubjectsCount = allGradesForStudent.size
        )
    }

    data class ClassGpaTrendSummary(
        val className: String,
        val classAverageGpa: Double,
        val term1AverageGpa: Double?,
        val term2AverageGpa: Double?,
        val term3AverageGpa: Double?,
        val improvingStudentsCount: Int,
        val steadyStudentsCount: Int,
        val decliningStudentsCount: Int,
        val totalStudentsCount: Int
    )

    /**
     * Calculates overall class-level GPA trend metrics.
     */
    fun computeClassGpaTrendSummary(
        className: String,
        classGrades: List<StudentGrade>,
        studentTrends: List<StudentGpaTrend>
    ): ClassGpaTrendSummary {
        val classAvgGpa = calculateGpaForGrades(classGrades)

        val t1Grades = classGrades.filter { it.academicTerm.contains("Term 1", ignoreCase = true) }
        val t2Grades = classGrades.filter { it.academicTerm.contains("Term 2", ignoreCase = true) }
        val t3Grades = classGrades.filter { it.academicTerm.contains("Term 3", ignoreCase = true) }

        val t1AvgGpa = if (t1Grades.isNotEmpty()) calculateGpaForGrades(t1Grades) else null
        val t2AvgGpa = if (t2Grades.isNotEmpty()) calculateGpaForGrades(t2Grades) else null
        val t3AvgGpa = if (t3Grades.isNotEmpty()) calculateGpaForGrades(t3Grades) else null

        val improving = studentTrends.count { it.trendDirection == GpaTrendDirection.IMPROVING }
        val steady = studentTrends.count { it.trendDirection == GpaTrendDirection.STEADY }
        val declining = studentTrends.count { it.trendDirection == GpaTrendDirection.DECLINING }

        return ClassGpaTrendSummary(
            className = className,
            classAverageGpa = classAvgGpa,
            term1AverageGpa = t1AvgGpa,
            term2AverageGpa = t2AvgGpa,
            term3AverageGpa = t3AvgGpa,
            improvingStudentsCount = improving,
            steadyStudentsCount = steady,
            decliningStudentsCount = declining,
            totalStudentsCount = studentTrends.size
        )
    }
}
