console.log('Villadictos Hotel script loaded');

// Mobile Menu Toggle
document.addEventListener('DOMContentLoaded', function () {
    const mobileToggle = document.querySelector('.mobile-menu-toggle');
    const mainNav = document.querySelector('.main-nav');
    const authButtons = document.querySelector('.auth-buttons');
    const overlay = document.querySelector('.mobile-overlay');

    if (mobileToggle) {
        mobileToggle.addEventListener('click', function () {
            mobileToggle.classList.toggle('active');
            mainNav.classList.toggle('active');
            authButtons.classList.toggle('active');
            if (overlay) overlay.classList.toggle('active');
            document.body.style.overflow = mainNav.classList.contains('active') ? 'hidden' : '';
        });
    }

    // Close menu when clicking overlay
    if (overlay) {
        overlay.addEventListener('click', function () {
            mobileToggle.classList.remove('active');
            mainNav.classList.remove('active');
            authButtons.classList.remove('active');
            overlay.classList.remove('active');
            document.body.style.overflow = '';
        });
    }

    // Close menu when clicking a nav link
    const navLinks = document.querySelectorAll('.main-nav a');
    navLinks.forEach(link => {
        link.addEventListener('click', function () {
            if (window.innerWidth <= 768) {
                mobileToggle.classList.remove('active');
                mainNav.classList.remove('active');
                authButtons.classList.remove('active');
                if (overlay) overlay.classList.remove('active');
                document.body.style.overflow = '';
            }
        });
    });

    // Handle window resize
    window.addEventListener('resize', function () {
        if (window.innerWidth > 768) {
            mobileToggle?.classList.remove('active');
            mainNav?.classList.remove('active');
            authButtons?.classList.remove('active');
            overlay?.classList.remove('active');
            document.body.style.overflow = '';
        }
    });
});
