/*
 * Processing Companion App for Arduino Pressure Sensor System
 * 
 * This Processing sketch receives data from the Arduino via serial port, 
 * captures screenshots, and provides a visualization interface for the 
 * pressure sensor data. It also allows for saving screenshots and viewing
 * exported CSV files.
 */

import processing.serial.*;

Serial arduino;
String[] ports;
PFont f;
boolean connected = false;

// UI elements
int buttonW = 180;
int buttonH = 40;

// Data storage
String[][] csvData;
int dataRows = 0;
String[] sensorData = new String[15];
String timestamp = "";
boolean receivingScreenshot = false;
boolean receivingCSV = false;
ArrayList<String> csvLines = new ArrayList<String>();
String currentCSVFilename = "";

// Display mode
int displayMode = 0; // 0 = main, 1 = screenshot, 2 = csv view

void setup() {
  size(800, 600);
  surface.setTitle("Pressure Sensor Visualization Companion");
  
  // Initialize font
  f = createFont("Arial", 16, true);
  textFont(f);
  
  // Get list of available serial ports
  ports = Serial.list();
  
  // Initialize empty sensor data
  for (int i = 0; i < 15; i++) {
    sensorData[i] = "0";
  }
}

void draw() {
  background(40);
  
  // Handle different display modes
  if (displayMode == 0) {
    drawMainScreen();
  } else if (displayMode == 1) {
    drawScreenshotView();
  } else if (displayMode == 2) {
    drawCSVView();
  }
}

void drawMainScreen() {
  fill(255);
  textAlign(CENTER, TOP);
  textSize(24);
  text("Pressure Sensor Visualization Companion", width/2, 20);
  
  if (!connected) {
    // Connection panel
    drawConnectionPanel();
  } else {
    // Connected interface
    drawConnectedInterface();
  }
}

void drawConnectionPanel() {
  textSize(18);
  text("Select Arduino Serial Port:", width/2, 80);
  
  for (int i = 0; i < ports.length; i++) {
    // Draw port selection buttons
    fill(100);
    rect(width/2 - buttonW/2, 120 + i*50, buttonW, buttonH);
    fill(255);
    textSize(16);
    text(ports[i], width/2, 120 + i*50 + buttonH/2 - 8);
  }
  
  textSize(14);
  fill(200);
  text("Click on a port to connect to your Arduino", width/2, height - 60);
}

void drawConnectedInterface() {
  // Draw sensor visualization
  drawSensorGrid();
  
  // Draw control buttons
  drawControlButtons();
  
  // Status info
  fill(255);
  textAlign(LEFT, TOP);
  textSize(16);
  text("Connected to: " + arduino.port.getPortName(), 20, 570);
  
  // Draw legend
  drawLegend();
}

void drawSensorGrid() {
  textAlign(CENTER, CENTER);
  textSize(16);
  
  // Draw grid for 15 sensors (5x3)
  for (int i = 0; i < 15; i++) {
    int x = 120 + (i % 5) * 120;
    int y = 100 + (i / 5) * 120;
    
    // Draw sensor circle with color based on value
    int value = int(sensorData[i]);
    color sensorColor = getSensorColor(value);
    
    fill(sensorColor);
    ellipse(x, y, 80, 80);
    
    // Sensor number
    fill(0);
    textSize(14);
    text("S" + (i+1), x, y - 25);
    
    // Sensor value
    textSize(18);
    text(value, x, y);
  }
  
  // Draw timestamp if available
  if (timestamp.length() > 0) {
    fill(255);
    textAlign(RIGHT, TOP);
    textSize(18);
    text("Time: " + timestamp, width - 20, 20);
  }
}

color getSensorColor(int value) {
  // Determine color based on pressure value
  // Matching the Arduino thresholds
  if (value < 200) {
    return color(0, 255, 0); // Green
  } else if (value < 500) {
    return color(255, 255, 0); // Yellow
  } else if (value < 800) {
    return color(255, 165, 0); // Orange
  } else {
    return color(255, 0, 0); // Red
  }
}

void drawControlButtons() {
  textAlign(CENTER, CENTER);
  textSize(16);
  
  // Screenshot button
  fill(100, 100, 200);
  rect(620, 480, buttonW, buttonH);
  fill(255);
  text("Take Screenshot", 620 + buttonW/2, 480 + buttonH/2);
  
  // Export CSV button
  fill(100, 200, 100);
  rect(620, 530, buttonW, buttonH);
  fill(255);
  text("Export CSV Data", 620 + buttonW/2, 530 + buttonH/2);
}

void drawLegend() {
  int legendY = 500;
  int startX = 50;
  
  textAlign(LEFT, CENTER);
  textSize(16);
  fill(255);
  text("Legend:", startX, legendY);
  
  // Green - Low pressure
  fill(0, 255, 0);
  ellipse(startX + 100, legendY, 20, 20);
  fill(255);
  text("Low", startX + 120, legendY);
  
  // Yellow - Medium pressure
  fill(255, 255, 0);
  ellipse(startX + 170, legendY, 20, 20);
  fill(255);
  text("Medium", startX + 190, legendY);
  
  // Orange - High pressure
  fill(255, 165, 0);
  ellipse(startX + 270, legendY, 20, 20);
  fill(255);
  text("High", startX + 290, legendY);
  
  // Red - Very high pressure
  fill(255, 0, 0);
  ellipse(startX + 360, legendY, 20, 20);
  fill(255);
  text("Very High", startX + 380, legendY);
}

void drawScreenshotView() {
  background(40);
  
  // Title
  fill(255);
  textAlign(CENTER, TOP);
  textSize(24);
  text("Screenshot View", width/2, 20);
  
  // Draw sensor visualization
  drawSensorGrid();
  
  // Back button
  fill(150);
  rect(20, 20, 100, 40);
  fill(255);
  textAlign(CENTER, CENTER);
  textSize(16);
  text("Back", 70, 40);
  
  // Save button
  fill(100, 200, 100);
  rect(width - 120, 20, 100, 40);
  fill(255);
  text("Save", width - 70, 40);
}

void drawCSVView() {
  background(40);
  
  // Title with filename
  fill(255);
  textAlign(CENTER, TOP);
  textSize(24);
  text("CSV Data: " + currentCSVFilename, width/2, 20);
  
  // Back button
  fill(150);
  rect(20, 20, 100, 40);
  fill(255);
  textAlign(CENTER, CENTER);
  textSize(16);
  text("Back", 70, 40);
  
  // Draw table
  drawCSVTable();
}

void drawCSVTable() {
  if (csvData == null || csvData.length == 0) {
    textAlign(CENTER, CENTER);
    text("No CSV data available", width/2, height/2);
    return;
  }
  
  int startY = 80;
  int rowHeight = 30;
  int colWidth = 60;
  int startX = 50;
  
  // Draw headers
  fill(100, 100, 200);
  for (int i = 0; i < csvData[0].length; i++) {
    rect(startX + i*colWidth, startY, colWidth, rowHeight);
    fill(255);
    textAlign(CENTER, CENTER);
    textSize(14);
    text(csvData[0][i], startX + i*colWidth + colWidth/2, startY + rowHeight/2);
    fill(100, 100, 200);
  }
  
  // Draw data rows
  int maxRows = min(csvData.length - 1, 16); // Limit to 16 rows
  for (int row = 0; row < maxRows; row++) {
    for (int col = 0; col < csvData[0].length; col++) {
      if (row % 2 == 0) {
        fill(70);
      } else {
        fill(90);
      }
      rect(startX + col*colWidth, startY + (row+1)*rowHeight, colWidth, rowHeight);
      fill(255);
      textSize(12);
      text(csvData[row+1][col], startX + col*colWidth + colWidth/2, startY + (row+1)*rowHeight + rowHeight/2);
    }
  }
  
  // Show row count
  fill(255);
  textAlign(RIGHT, BOTTOM);
  text("Showing " + maxRows + " of " + (csvData.length - 1) + " rows", width - 50, height - 20);
}

void mousePressed() {
  if (!connected) {
    // Check if a port button was clicked
    for (int i = 0; i < ports.length; i++) {
      if (mouseX > width/2 - buttonW/2 && mouseX < width/2 + buttonW/2 &&
          mouseY > 120 + i*50 && mouseY < 120 + i*50 + buttonH) {
        connectToPort(i);
        break;
      }
    }
  } else if (displayMode == 0) {
    // Main interface buttons
    
    // Screenshot button
    if (mouseX > 620 && mouseX < 620 + buttonW && 
        mouseY > 480 && mouseY < 480 + buttonH) {
      requestScreenshot();
    }
    
    // Export CSV button
    if (mouseX > 620 && mouseX < 620 + buttonW && 
        mouseY > 530 && mouseY < 530 + buttonH) {
      requestCSVExport();
    }
  } else if (displayMode == 1) {
    // Screenshot view buttons
    
    // Back button
    if (mouseX > 20 && mouseX < 120 && mouseY > 20 && mouseY < 60) {
      displayMode = 0;
    }
    
    // Save button
    if (mouseX > width - 120 && mouseX < width - 20 && mouseY > 20 && mouseY < 60) {
      saveScreenshot();
    }
  } else if (displayMode == 2) {
    // CSV view buttons
    
    // Back button
    if (mouseX > 20 && mouseX < 120 && mouseY > 20 && mouseY < 60) {
      displayMode = 0;
    }
  }
}

void connectToPort(int index) {
  try {
    arduino = new Serial(this, ports[index], 115200);
    arduino.bufferUntil('\n');
    connected = true;
    println("Connected to " + ports[index]);
  } catch (Exception e) {
    println("Error connecting to port: " + e.getMessage());
  }
}

void serialEvent(Serial port) {
  // Read the incoming serial data
  String inData = port.readString().trim();
  
  if (receivingScreenshot) {
    // Process screenshot data
    if (inData.equals("SCREENSHOT_END")) {
      receivingScreenshot = false;
      displayMode = 1; // Switch to screenshot view
      return;
    }
    
    if (inData.startsWith("TIME:")) {
      timestamp = inData.substring(5);
    } else if (inData.startsWith("DATA:")) {
      String dataStr = inData.substring(5);
      sensorData = dataStr.split(",");
    }
  } else if (receivingCSV) {
    // Process CSV data
    if (inData.equals("CSV_EXPORT_END")) {
      receivingCSV = false;
      processCSVData();
      displayMode = 2; // Switch to CSV view
      return;
    }
    
    if (inData.startsWith("FILENAME:")) {
      currentCSVFilename = inData.substring(9);
    } else {
      csvLines.add(inData);
    }
  } else {
    // Check for special commands
    if (inData.equals("SCREENSHOT_BEGIN")) {
      receivingScreenshot = true;
      println("Receiving screenshot data...");
    } else if (inData.equals("CSV_EXPORT_BEGIN")) {
      receivingCSV = true;
      csvLines.clear();
      println("Receiving CSV data...");
    } else if (inData.startsWith("DATA:")) {
      // Regular data update
      String dataStr = inData.substring(5);
      sensorData = dataStr.split(",");
    }
  }
}

void requestScreenshot() {
  if (connected) {
    arduino.write("SCREENSHOT\n");
    println("Requested screenshot");
  }
}

void requestCSVExport() {
  if (connected) {
    arduino.write("EXPORT_CSV\n");
    println("Requested CSV export");
  }
}

void saveScreenshot() {
  String filename = "screenshot_" + timestamp.replace(':', '-') + ".png";
  save(filename);
  println("Screenshot saved as " + filename);
}

void processCSVData() {
  if (csvLines.size() > 0) {
    // Convert ArrayList to array
    String[] csvArray = csvLines.toArray(new String[0]);
    
    // Split each line into values
    csvData = new String[csvArray.length][];
    for (int i = 0; i < csvArray.length; i++) {
      csvData[i] = csvArray[i].split(",");
    }
    
    println("Processed " + csvData.length + " rows of CSV data");
  } else {
    csvData = null;
  }
}
