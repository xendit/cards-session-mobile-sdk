#!/bin/bash

# Get the script directory and project root directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# Configuration
PODSPEC_NAME="CardSessionMobileSDK.podspec"
PODSPEC_PATH="${PROJECT_ROOT}/${PODSPEC_NAME}"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print step information
print_step() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

# Function to check if command succeeded
check_status() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ $1 completed successfully${NC}"
    else
        echo -e "${RED}✗ $1 failed${NC}"
        exit 1
    fi
}

# Function to check if pod is installed
check_pod_installation() {
    if ! command -v pod &> /dev/null; then
        echo -e "${RED}Error: CocoaPods is not installed${NC}"
        echo "Please install CocoaPods using: gem install cocoapods"
        exit 1
    fi
}

# Function to validate podspec
validate_podspec() {
    print_step "Validating podspec"

    if [ ! -f "$PODSPEC_PATH" ]; then
        echo -e "${RED}Error: Podspec not found at: $PODSPEC_PATH${NC}"
        exit 1
    fi

    pod spec lint "$PODSPEC_PATH" --allow-warnings
    check_status "Podspec validation"
}

# Function to push to trunk
push_to_trunk() {
    print_step "Pushing to CocoaPods Trunk"

    # Adding --verbose flag for detailed output
    pod trunk push "$PODSPEC_PATH" --allow-warnings --verbose
    check_status "Pod trunk push"
}

# Main execution
main() {
    # Check if cocoapods is installed
    check_pod_installation

    # First validate the podspec
    validate_podspec

    # Then push to trunk
    push_to_trunk

    echo -e "\n${GREEN}🎉 Successfully published podspec to CocoaPods Trunk!${NC}"
}

# Run the script
main