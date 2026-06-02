/* footer.js - Component file for rendering the global application layout footer */

/**
 * Dynamic generation function responsible for injecting the global template blueprint
 * into the page's designated baseline placeholder anchor.
 */
function renderFooter() {
    // 1. Locate the target layout insertion placeholder
    const footerDiv = document.getElementById("footer");
    
    // Safety check to prevent execution errors if a page omits the footer anchor
    if (!footerDiv) {
        return;
    }

    // 2. Define the static semantic template layout block
    footerDiv.innerHTML = `
        <footer class="footer">
            <div class="footer-container">
                
                <div class="footer-logo">
                    <img src="../assets/images/logo/logo.png" alt="Hospital CMS Logo">
                    <p>© Copyright 2025. All Rights Reserved by Hospital CMS.</p>
                </div>
                
                <div class="footer-links">
                    
                    <div class="footer-column">
                        <h4>Company</h4>
                        <a href="#">About</a>
                        <a href="#">Careers</a>
                        <a href="#">Press</a>
                    </div>
                    
                    <div class="footer-column">
                        <h4>Support</h4>
                        <a href="#">Account</a>
                        <a href="#">Help Center</a>
                        <a href="#">Contact Us</a>
                    </div>
                    
                    <div class="footer-column">
                        <h4>Legals</h4>
                        <a href="#">Terms &amp; Conditions</a>
                        <a href="#">Privacy Policy</a>
                        <a href="#">Licensing</a>
                    </div>
                    
                </div> </div> </footer>
    `;
}

// 3. Execution Binding Strategy
// Automatically trigger the paint cycle as soon as the DOM engine completes basic parsing.
if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", renderFooter);
} else {
    renderFooter(); // Run immediately if the script was loaded late or asynchronously
}