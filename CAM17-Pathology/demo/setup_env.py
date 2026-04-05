import os
import sys
import subprocess

def run_command(cmd):
    print(f'Running: {cmd}')
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if result.returncode != 0:
        print(f'Error: {result.stderr}')
        return False
    print(result.stdout)
    return True

def check_python_version():
    print(f'Python version: {sys.version}')
    if sys.version_info < (3, 8):
        print('Error: Python 3.8+ is required')
        return False
    return True

def install_dependencies():
    print('Installing dependencies...')
    return run_command('pip install -r requirements.txt')

def check_openslide():
    print('Checking OpenSlide installation...')
    try:
        import openslide
        print(f'OpenSlide Python version: {openslide.__version__}')
        return True
    except ImportError:
        print('Warning: openslide-python not found. Using sample data instead.')
        return True

def check_gpu():
    print('Checking GPU availability...')
    try:
        import torch
        has_gpu = torch.cuda.is_available()
        print(f'GPU available: {has_gpu}')
        if has_gpu:
            print(f'GPU device: {torch.cuda.get_device_name(0)}')
        return True
    except ImportError:
        print('Error: torch not found')
        return False

def create_directories():
    print('Creating necessary directories...')
    directories = ['data', 'checkpoints', 'visualizations', 'data/camelyon17_processed']
    for dir_path in directories:
        os.makedirs(dir_path, exist_ok=True)
    print('Directories created successfully')
    return True

def main():
    print('=== Setting up CAMELYON17 MIL DEMO Environment ===')
    
    steps = [
        ('Check Python version', check_python_version),
        ('Install dependencies', install_dependencies),
        ('Check OpenSlide', check_openslide),
        ('Check GPU', check_gpu),
        ('Create directories', create_directories)
    ]
    
    all_passed = True
    for step_name, step_func in steps:
        print(f'\n--- {step_name} ---')
        if not step_func():
            all_passed = False
    
    if all_passed:
        print('\n=== Environment setup completed successfully! ===')
        print('You can now run the demo with:')
        print('python run_demo.py')
    else:
        print('\n=== Environment setup failed! ===')
        print('Please check the errors above and try again.')
        sys.exit(1)

if __name__ == '__main__':
    main()
