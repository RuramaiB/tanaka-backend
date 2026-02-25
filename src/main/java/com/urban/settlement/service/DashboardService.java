package com.urban.settlement.service;

import com.urban.settlement.model.Issue;
import com.urban.settlement.model.Task;
import com.urban.settlement.model.enums.IssueCategory;
import com.urban.settlement.model.enums.IssueStatus;
import com.urban.settlement.repository.IssueRepository;
import com.urban.settlement.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard analytics service
 * 
 * Objective 5: Real-time analytics dashboard
 * Provides KPIs and aggregated metrics
 */
@Service
public class DashboardService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Get comprehensive dashboard metrics
     */
    public DashboardMetricsDTO getMetrics() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusWeeks(1);
        LocalDateTime startOfMonth = now.minusMonths(1);

        // Total reports
        long totalReports = issueRepository.count();
        long todayReports = issueRepository.findByReportedAtBetween(startOfDay, now).size();
        long weekReports = issueRepository.findByReportedAtBetween(startOfWeek, now).size();
        long monthReports = issueRepository.findByReportedAtBetween(startOfMonth, now).size();

        // Status counts
        long pendingCount = issueRepository.countByStatus(IssueStatus.PENDING);
        long inProgressCount = issueRepository.countByStatus(IssueStatus.IN_PROGRESS);
        long resolvedCount = issueRepository.countByStatus(IssueStatus.RESOLVED);
        long rejectedCount = issueRepository.countByStatus(IssueStatus.REJECTED);

        // Average response time (reportedAt -> assignedAt)
        double avgResponseTime = calculateAverageResponseTime();

        // Average resolution time (assignedAt -> completedAt)
        double avgResolutionTime = calculateAverageResolutionTime();

        // Resolution rate
        double resolutionRate = totalReports > 0
                ? (double) resolvedCount / totalReports * 100
                : 0.0;

        // Cost per resolution
        double costPerResolution = calculateCostPerResolution();

        // Issues by category
        Map<String, Long> issuesByCategory = new HashMap<>();
        for (IssueCategory category : IssueCategory.values()) {
            long count = issueRepository.countByCategory(category);
            issuesByCategory.put(category.name(), count);
        }

        // Issues by severity
        Map<String, Long> issuesBySeverity = new HashMap<>();
        for (com.urban.settlement.model.enums.Severity severity : com.urban.settlement.model.enums.Severity.values()) {
            long count = issueRepository.findBySeverity(severity).size();
            issuesBySeverity.put(severity.name(), count);
        }

        return new DashboardMetricsDTO(
                totalReports,
                todayReports,
                weekReports,
                monthReports,
                pendingCount,
                inProgressCount,
                resolvedCount,
                rejectedCount,
                avgResponseTime,
                avgResolutionTime,
                resolutionRate,
                costPerResolution,
                issuesByCategory,
                issuesBySeverity);
    }

    /**
     * Calculate average response time (hours)
     */
    private double calculateAverageResponseTime() {
        List<Issue> assignedIssues = issueRepository.findAll().stream()
                .filter(issue -> issue.getAssignedOfficerId() != null)
                .toList();

        if (assignedIssues.isEmpty()) {
            return 0.0;
        }

        long totalHours = 0;
        int count = 0;

        for (Issue issue : assignedIssues) {
            List<Task> tasks = taskRepository.findByIssueId(issue.getId());
            if (!tasks.isEmpty()) {
                Task task = tasks.get(0);
                long hours = ChronoUnit.HOURS.between(
                        issue.getReportedAt(), task.getAssignedAt());
                totalHours += hours;
                count++;
            }
        }

        return count > 0 ? (double) totalHours / count : 0.0;
    }

    /**
     * Calculate average resolution time (hours)
     */
    private double calculateAverageResolutionTime() {
        List<Task> completedTasks = taskRepository.findAll().stream()
                .filter(task -> task.getCompletedAt() != null)
                .toList();

        if (completedTasks.isEmpty()) {
            return 0.0;
        }

        long totalMinutes = completedTasks.stream()
                .filter(task -> task.getActualDuration() != null)
                .mapToLong(Task::getActualDuration)
                .sum();

        return totalMinutes / 60.0 / completedTasks.size();
    }

    /**
     * Calculate cost per resolution
     */
    private double calculateCostPerResolution() {
        List<Task> completedTasks = taskRepository.findAll().stream()
                .filter(task -> task.getCompletedAt() != null && task.getCost() != null)
                .toList();

        if (completedTasks.isEmpty()) {
            return 0.0;
        }

        double totalCost = completedTasks.stream()
                .mapToDouble(Task::getCost)
                .sum();

        return totalCost / completedTasks.size();
    }

    /**
     * Get heatmap data for Leaflet
     */
    public List<double[]> getHeatmapData() {
        List<Issue> issues = issueRepository.findAll();

        return issues.stream()
                .filter(issue -> issue.getLocation() != null)
                .map(issue -> new double[] {
                        issue.getLocation().getY(), // latitude
                        issue.getLocation().getX(), // longitude
                        getSeverityIntensity(issue.getSeverity()) // intensity
                })
                .toList();
    }

    /**
     * Convert severity to heatmap intensity
     */
    private double getSeverityIntensity(com.urban.settlement.model.enums.Severity severity) {
        if (severity == null)
            return 0.25;
        return switch (severity) {
            case LOW -> 0.25;
            case MEDIUM -> 0.5;
            case HIGH -> 0.75;
            case CRITICAL -> 1.0;
        };
    }

    /**
     * Dashboard metrics DTO
     */
    public static class DashboardMetricsDTO {
        private final long totalReports;
        private final long todayReports;
        private final long weekReports;
        private final long monthReports;
        private final long pendingCount;
        private final long inProgressCount;
        private final long resolvedCount;
        private final long rejectedCount;
        private final double avgResponseTime;
        private final double avgResolutionTime;
        private final double resolutionRate;
        private final double costPerResolution;
        private final Map<String, Long> issuesByCategory;
        private final Map<String, Long> issuesBySeverity;

        public DashboardMetricsDTO(long totalReports, long todayReports, long weekReports,
                long monthReports, long pendingCount, long inProgressCount,
                long resolvedCount, long rejectedCount, double avgResponseTime,
                double avgResolutionTime, double resolutionRate,
                double costPerResolution, Map<String, Long> issuesByCategory,
                Map<String, Long> issuesBySeverity) {
            this.totalReports = totalReports;
            this.todayReports = todayReports;
            this.weekReports = weekReports;
            this.monthReports = monthReports;
            this.pendingCount = pendingCount;
            this.inProgressCount = inProgressCount;
            this.resolvedCount = resolvedCount;
            this.rejectedCount = rejectedCount;
            this.avgResponseTime = avgResponseTime;
            this.avgResolutionTime = avgResolutionTime;
            this.resolutionRate = resolutionRate;
            this.costPerResolution = costPerResolution;
            this.issuesByCategory = issuesByCategory;
            this.issuesBySeverity = issuesBySeverity;
        }

        // Getters
        public long getTotalReports() {
            return totalReports;
        }

        public long getTodayReports() {
            return todayReports;
        }

        public long getWeekReports() {
            return weekReports;
        }

        public long getMonthReports() {
            return monthReports;
        }

        public long getPendingCount() {
            return pendingCount;
        }

        public long getInProgressCount() {
            return inProgressCount;
        }

        public long getResolvedCount() {
            return resolvedCount;
        }

        public long getRejectedCount() {
            return rejectedCount;
        }

        public double getAvgResponseTime() {
            return avgResponseTime;
        }

        public double getAvgResolutionTime() {
            return avgResolutionTime;
        }

        public double getResolutionRate() {
            return resolutionRate;
        }

        public double getCostPerResolution() {
            return costPerResolution;
        }

        public Map<String, Long> getIssuesByCategory() {
            return issuesByCategory;
        }

        public Map<String, Long> getIssuesBySeverity() {
            return issuesBySeverity;
        }
    }
}
