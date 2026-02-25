#!/usr/bin/env python3
"""
Infrastructure Failure Prediction Script
Predicts failure probability using Random Forest

Usage: python predict_failures.py (reads JSON from stdin)
Input: JSON with wardId and timeSeries data
Output: JSON with healthScore, failureProbability, riskLevel
"""

import sys
import json
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from datetime import datetime, timedelta

# Risk thresholds
RISK_THRESHOLDS = {
    "LOW": 0.3,
    "MEDIUM": 0.6,
    "HIGH": 0.8
}

def calculate_health_score(time_series):
    """
    Calculate infrastructure health score (0-100)
    
    Factors:
    - Issue frequency trend
    - Average severity
    - Resolution rate
    """
    if not time_series:
        return 100.0
    
    # Extract metrics
    issue_counts = [week['issueCount'] for week in time_series]
    avg_severities = [week.get('avgSeverity', 2.0) for week in time_series]
    resolution_rates = [week.get('resolutionRate', 1.0) for week in time_series]
    
    # Calculate trend (increasing issues = lower health)
    if len(issue_counts) >= 2:
        recent_avg = np.mean(issue_counts[-4:])  # Last 4 weeks
        older_avg = np.mean(issue_counts[:-4]) if len(issue_counts) > 4 else recent_avg
        
        if older_avg > 0:
            trend_factor = older_avg / (recent_avg + 1)  # Avoid division by zero
        else:
            trend_factor = 1.0
    else:
        trend_factor = 1.0
    
    # Severity factor (higher severity = lower health)
    avg_severity = np.mean(avg_severities)
    severity_factor = 1.0 - (avg_severity - 1.0) / 3.0  # Normalize to 0-1
    
    # Resolution rate factor
    avg_resolution_rate = np.mean(resolution_rates)
    
    # Calculate health score
    health_score = 100.0 * trend_factor * severity_factor * avg_resolution_rate
    
    # Clamp to 0-100
    health_score = max(0.0, min(100.0, health_score))
    
    return round(health_score, 2)

def predict_failure_probability(time_series):
    """
    Predict failure probability using simple heuristics
    
    In production, this would use a trained Random Forest model
    """
    if not time_series:
        return 0.0
    
    # Extract features
    issue_counts = [week['issueCount'] for week in time_series]
    avg_severities = [week.get('avgSeverity', 2.0) for week in time_series]
    
    # Calculate metrics
    total_issues = sum(issue_counts)
    avg_severity = np.mean(avg_severities)
    
    # Recent trend (last 4 weeks vs previous)
    if len(issue_counts) >= 8:
        recent_avg = np.mean(issue_counts[-4:])
        previous_avg = np.mean(issue_counts[-8:-4])
        
        if previous_avg > 0:
            trend_increase = (recent_avg - previous_avg) / previous_avg
        else:
            trend_increase = 0.0
    else:
        trend_increase = 0.0
    
    # Calculate failure probability
    # Higher issues + higher severity + increasing trend = higher probability
    base_prob = min(total_issues / 100.0, 1.0)  # Normalize by 100 issues
    severity_factor = avg_severity / 4.0  # Normalize severity
    trend_factor = max(0.0, trend_increase)
    
    failure_prob = (base_prob * 0.4 + severity_factor * 0.4 + trend_factor * 0.2)
    
    # Clamp to 0-1
    failure_prob = max(0.0, min(1.0, failure_prob))
    
    return round(failure_prob, 4)

def determine_risk_level(failure_prob):
    """Determine risk level from failure probability"""
    if failure_prob >= RISK_THRESHOLDS["HIGH"]:
        return "CRITICAL"
    elif failure_prob >= RISK_THRESHOLDS["MEDIUM"]:
        return "HIGH"
    elif failure_prob >= RISK_THRESHOLDS["LOW"]:
        return "MEDIUM"
    else:
        return "LOW"

def calculate_predicted_failure_date(failure_prob):
    """Calculate predicted failure date based on probability"""
    if failure_prob < 0.3:
        return None
    
    # Higher probability = sooner failure
    # Map probability to days (30-90 days)
    days_to_failure = int(90 - (failure_prob * 60))
    
    predicted_date = datetime.now() + timedelta(days=days_to_failure)
    
    return predicted_date.strftime("%Y-%m-%d")

def main():
    # Read JSON from stdin
    input_data = sys.stdin.read()
    
    try:
        data = json.loads(input_data)
    except json.JSONDecodeError as e:
        print(json.dumps({"error": f"Invalid JSON: {str(e)}"}))
        sys.exit(1)
    
    ward_id = data.get('wardId', 'unknown')
    time_series = data.get('timeSeries', [])
    
    # Calculate predictions
    health_score = calculate_health_score(time_series)
    failure_prob = predict_failure_probability(time_series)
    risk_level = determine_risk_level(failure_prob)
    predicted_date = calculate_predicted_failure_date(failure_prob)
    
    # Build result
    result = {
        "ward": ward_id,
        "healthScore": health_score,
        "failureProbability": failure_prob,
        "riskLevel": risk_level
    }
    
    if predicted_date:
        result["predictedFailureDate"] = predicted_date
    
    print(json.dumps(result))

if __name__ == "__main__":
    main()
