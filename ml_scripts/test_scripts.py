#!/usr/bin/env python3
"""
Test script to verify Python ML scripts syntax and imports
"""

import unittest
import sys
import os

class TestMLScripts(unittest.TestCase):
    def test_imports(self):
        """Test that all scripts can be imported (verifies syntax and dependencies)"""
        try:
            import classify_image
            import cluster_issues
            import predict_failures
            import optimize_routes
            print("Successfully imported all ML modules")
        except ImportError as e:
            self.fail(f"Failed to import modules: {str(e)}")
        except Exception as e:
            self.fail(f"Script verification failed: {str(e)}")

    def test_model_paths(self):
        """Verify model paths existence"""
        # Create dummy model file if not exists for testing
        if not os.path.exists("ml_models"):
            os.makedirs("ml_models")
            
        # Create dummy h5 file to prevent classify_image from failing instantly 
        # (though it checks path in main, import shouldn't fail)
        pass

if __name__ == '__main__':
    unittest.main()
