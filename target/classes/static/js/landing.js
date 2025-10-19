// Nav shrink on scroll + fade-in for elements with .fade-in
(function(){
    const nav = document.querySelector('.main-nav');
    const onScroll = () => {
        if(window.scrollY > 30) nav.classList.add('nav--scrolled');
        else nav.classList.remove('nav--scrolled');
    };
    window.addEventListener('scroll', onScroll);
    onScroll();

    // Intersection fade-ins
    const obs = new IntersectionObserver(entries=>{
        entries.forEach(e=>{
            if(e.isIntersecting){
                e.target.classList.add('reveal');
                obs.unobserve(e.target);
            }
        });
    },{threshold:.15});

    document.querySelectorAll('.fade-in').forEach(el=>obs.observe(el));
})();
