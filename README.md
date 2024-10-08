# CropEgde Studio 🖼️

CropEdge Studio is an intuitive image processing tool designed to perform both edge detection and cropping on a variety of image formats.

It integrates multiple edge detection algorithms and a user-friendly cropping interface to handle both individual and batch image processing.


## Features

#### *1) Edge Detection*

Supports 3 powerful edge detection algorithms:

- *Roberts Cross Algorithm:* Simple and fast edge detection, effective for sharp edges.
- *Sobel Edge Detection:* Efficient detection of gradients in x and y directions.
- *Laplacian Edge Detection:* Uses second derivatives for edge detection, capturing unique features.

Key capabilities:

- Customizable parameters for each algorithm
- Batch processing support to handle multiple images or a ZIP archive
- Output results as processed image files

#### *2) Cropping*

- Interactive cropping interface with a resizeable crop box
- Supports individual and batch cropping, with the ability to adjust settings for each image
- Output results to the user-specified directory


## Technical details

- Language: Java
- GUI Framework: JavaFX
- Image Processing: Custom implementations of edge detection algorithms
- Concurrency: Utilizes Java's ExecutorService for efficient batch processing
- File Handling: Supports various image formats (PNG, JPG, JPEG) and ZIP archives


## Key Components

- *EdgeDetectionController:* Manages the edge detection UI and processes user interactions.
- *CropController:* Handles the cropping interface and operations.
- *BatchProcessorService:* Implements multi-threaded batch processing for both edge detection and cropping.

Additional Features
- Drag and Drop
- Multi-image Navigation
- Real-time Parameter Adjustment
- Comprehensive Error Handling


## Authors

- [@Pongpiphat Kalasuk](https://github.com/KongPongpi)
- [@Watcharapong Wanna](https://github.com/jaytakuz)


# CropEgde Studio

Our proudly present Project "CropEdge Studio" was developed as part of the SE233 Advanced Programming course at Chiang Mai University, College of Arts, Media and Technology.