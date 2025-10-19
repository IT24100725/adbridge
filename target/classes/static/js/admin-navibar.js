// Three dots menu dropdown toggle for admin navbar
document.addEventListener('DOMContentLoaded', function () {
    var moreBtn = document.getElementById('admin-more-btn');
    var dropdown = document.getElementById('admin-more-dropdown');
    var opened = false;

    function closeMenu(e) {
        if (!dropdown.contains(e.target) && e.target !== moreBtn) {
            dropdown.style.display = 'none';
            opened = false;
            document.removeEventListener('mousedown', closeMenu);
        }
    }

    moreBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        opened = !opened;
        dropdown.style.display = opened ? 'block' : 'none';
        if (opened) {
            setTimeout(() => {
                document.addEventListener('mousedown', closeMenu);
            }, 0);
        } else {
            document.removeEventListener('mousedown', closeMenu);
        }
    });
});