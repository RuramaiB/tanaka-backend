# Urban Settlement Mapping and Service Management System

**HIT 400 - Final Year Project**

A production-ready web-based system for intelligent infrastructure issue reporting, AI-powered classification, spatial analysis, predictive maintenance, and optimized resource allocation.

---

## 🎯 Project Objectives

### ✅ Objective 1: Automatic Classification & Severity Prediction
- **AI Model**: CNN (ResNet50) for pothole detection
- **Dataset**: Kaggle Pothole Segmentation Dataset
- **Target Accuracy**: 92%+
- **Processing Time**: 2-3 seconds
- **Features**: Auto-categorization (Road Damage, Drainage, Waste, Traffic Light) and severity prediction (Low, Medium, High, Critical)

### ✅ Objective 2: Hotspot Detection & Clustering
- **Algorithm**: DBSCAN (Density-Based Spatial Clustering)
- **Parameters**: ε = 1km, MinPoints = 3
- **Features**: 
  - Automatic grouping of nearby similar issues
  - Duplicate detection using TF-IDF cosine similarity
  - Heatmap visualization support
  - Prevents duplicate resource allocation

### ✅ Objective 3: Infrastructure Failure Prediction
- **Algorithm**: Random Forest Classifier
- **Forecast Window**: 30-90 days
- **Target Accuracy**: 85%+
- **Features**:
  - Health score calculation (0-100)
  - Failure probability prediction
  - Risk level classification (Low, Medium, High, Critical)
  - Preventive maintenance scheduling

### ✅ Objective 4: Intelligent Resource Optimization
- **Algorithms**:
  - Hungarian Algorithm for task assignment (O(n³))
  - OR-Tools VRP for route optimization
- **Features**:
  - Skill-based officer matching
  - Workload balancing
  - Proximity-based assignment
  - Minimized travel distance
  - Real-time task reallocation

### ✅ Objective 5: Real-Time Analytics Dashboard
- **KPIs**:
  - Total reports (today, week, month)
  - Pending vs resolved counts
  - Average response/resolution time
  - Resolution rate percentage
  - Cost per resolution
  - Officer performance metrics
  - Category/severity distribution

---

## 🏗️ System Architecture

### Backend Stack
- **Framework**: Spring Boot 3.2.2
- **Database**: MongoDB with geospatial indexing
- **Authentication**: JWT (already implemented)
- **Language**: Java 17

### ML Pipeline
- **Language**: Python 3.x
- **Integration**: ProcessBuilder (Spring Boot → Python)
- **Libraries**: TensorFlow, scikit-learn, OR-Tools, SciPy

### Frontend (Guidance)
- **Mapping**: Leaflet.js with Google Maps tiles
- **Features**: Marker clustering, heatmap layer, ward boundaries

---

## 📁 Project Structure

```
tanaka/
├── src/main/java/com/urban/settlement/
│   ├── UrbanSettlementApplication.java
│   ├── model/
│   │   ├── Issue.java
│   │   ├── Officer.java
│   │   ├── Task.java
│   │   ├── Ward.java
│   │   ├── Cluster.java
│   │   ├── Prediction.java
│   │   └── enums/
│   ├── repository/
│   │   ├── IssueRepository.java
│   │   ├── OfficerRepository.java
│   │   ├── TaskRepository.java
│   │   ├── WardRepository.java
│   │   ├── ClusterRepository.java
│   │   └── PredictionRepository.java
│   ├── service/
│   │   ├── PythonExecutorService.java
│   │   ├── ImageClassificationService.java
│   │   ├── ClusteringService.java
│   │   ├── PredictionService.java
│   │   ├── OptimizationService.java
│   │   ├── IssueService.java
│   │   ├── OfficerService.java
│   │   ├── TaskService.java
│   │   ├── WardService.java
│   │   └── DashboardService.java
│   ├── controller/
│   │   ├── IssueController.java
│   │   ├── HotspotController.java
│   │   ├── PredictionController.java
│   │   ├── TaskController.java
│   │   ├── DashboardController.java
│   │   ├── OfficerController.java
│   │   └── WardController.java
│   └── config/
│       ├── MongoConfig.java
│       └── FileStorageConfig.java
├── ml_scripts/
│   ├── classify_image.py
│   ├── cluster_issues.py
│   ├── predict_failures.py
│   ├── optimize_routes.py
│   └── requirements.txt
├── uploads/issues/          # Image storage directory
├── ml_models/               # Trained ML models
└── pom.xml
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- MongoDB 4.4+
- Python 3.8+

### Installation

#### 1. Clone and Setup Backend
```bash
cd tanaka
mvn clean install
```

#### 2. Install Python Dependencies
```bash
cd ml_scripts
pip install -r requirements.txt
```

#### 3. Configure MongoDB
Edit `src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/urban_settlement
```

#### 4. Run Application
```bash
mvn spring-boot:run
```

Server starts on: `http://localhost:8080`

---

## 📡 API Endpoints

### Issue Management
- `POST /api/issues/classify` - Upload image and auto-classify
- `GET /api/issues` - Get all issues (paginated)
- `GET /api/issues/{id}` - Get issue by ID
- `PUT /api/issues/{id}/status` - Update issue status
- `GET /api/issues/nearby?lat={lat}&lng={lng}&radiusKm={radius}` - Find nearby issues

### Hotspot Detection
- `GET /api/hotspots` - Detect hotspots (DBSCAN clustering)
- `GET /api/hotspots/duplicates/{id}` - Find duplicate issues

### Failure Prediction
- `GET /api/predictions/failure-risk` - Get all ward predictions
- `GET /api/predictions/ward/{wardId}` - Get prediction for specific ward
- `POST /api/predictions/generate` - Generate new predictions

### Task Optimization
- `POST /api/tasks/optimize` - Optimize task assignment
- `GET /api/tasks/officer/{officerId}` - Get officer tasks
- `PUT /api/tasks/{id}/complete` - Complete task

### Dashboard Analytics
- `GET /api/dashboard/metrics` - Get comprehensive KPIs
- `GET /api/dashboard/heatmap` - Get heatmap data

### Officer Management
- `GET /api/officers` - Get all officers
- `GET /api/officers/available` - Get available officers
- `POST /api/officers` - Create officer

### Ward Management
- `GET /api/wards` - Get all wards
- `GET /api/wards/low-health?threshold={score}` - Get at-risk wards

---

## 🧪 Testing

### Test Image Classification
```bash
curl -X POST http://localhost:8080/api/issues/classify \
  -F "image=@pothole.jpg" \
  -F "title=Large pothole" \
  -F "description=Pothole on main road" \
  -F "latitude=-26.1" \
  -F "longitude=28.0"
```

### Test Hotspot Detection
```bash
curl http://localhost:8080/api/hotspots
```

### Test Failure Prediction
```bash
curl http://localhost:8080/api/predictions/failure-risk
```

### Test Route Optimization
```bash
curl -X POST http://localhost:8080/api/tasks/optimize \
  -H "Content-Type: application/json" \
  -d '{"issueIds": ["issue1", "issue2", "issue3"]}'
```

---

## 🎓 Academic Justification

### Algorithm Selection

#### 1. DBSCAN for Clustering
- **Why**: Handles arbitrary cluster shapes, no need to specify cluster count
- **Complexity**: O(n log n) with spatial indexing
- **Alternative Considered**: K-Means (rejected - requires predefined k)

#### 2. Random Forest for Prediction
- **Why**: Robust to overfitting, handles non-linear relationships
- **Complexity**: O(n log n × trees)
- **Alternative Considered**: LSTM (rejected - insufficient time-series data)

#### 3. Hungarian Algorithm for Assignment
- **Why**: Guarantees optimal assignment in polynomial time
- **Complexity**: O(n³)
- **Alternative Considered**: Greedy (rejected - suboptimal results)

#### 4. OR-Tools VRP for Routing
- **Why**: Industry-standard, handles constraints, near-optimal solutions
- **Complexity**: NP-hard (uses heuristics)
- **Alternative Considered**: Dijkstra (rejected - doesn't handle capacity constraints)

---

## 📊 Performance Metrics

| Operation | Target | Measurement |
|-----------|--------|-------------|
| Image Classification | < 3s | Response time |
| Clustering (1000 issues) | < 5s | Processing time |
| Failure Prediction | < 10s | Model inference |
| Route Optimization (50 tasks) | < 15s | OR-Tools execution |
| Dashboard Metrics | < 1s | MongoDB aggregation |

---

## 🔒 Security

- JWT authentication (already implemented)
- CORS enabled for frontend integration
- File upload validation
- Input sanitization

---

## 📝 Notes

- Images are stored in `./uploads/issues/` directory
- Only image paths are saved in MongoDB (not binary data)
- Python scripts must be executable and return JSON to stdout
- MongoDB geospatial indexes are auto-created on startup

---

## 👨‍💻 Development

### Add New ML Model
1. Train model and save to `ml_models/`
2. Create Python script in `ml_scripts/`
3. Add service in `com.urban.settlement.service`
4. Expose via controller

### Add New Endpoint
1. Create method in appropriate controller
2. Add `@GetMapping` or `@PostMapping` annotation
3. Document in this README

---

## 📚 Dataset

**Kaggle Pothole Segmentation Dataset**
- URL: https://www.kaggle.com/datasets/farzadnekouei/pothole-segmentation-for-road-damage-assessment
- Size: ~5000 images
- Usage: Train CNN model for road damage classification

---

## 🎯 Future Enhancements

- Real-time WebSocket updates for dashboard
- Mobile app integration
- Advanced ML models (LSTM for time-series)
- Multi-language support
- Citizen feedback system

---

## 📄 License

Academic Project - HIT 400 Final Year Project

---

## 👤 Author

**Student Name**: [Your Name]  
**Student ID**: [Your ID]  
**Course**: HIT 400 - Final Year Project  
**Institution**: [Your Institution]  
**Year**: 2026
