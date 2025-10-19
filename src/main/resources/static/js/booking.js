document.addEventListener('DOMContentLoaded', function(){
  const select = document.getElementById('serviceType');
  const groups = {
    TV_ADVERTISEMENT: 'group-tv',
    RADIO_PROMOTION: 'group-radio',
    SOCIAL_MEDIA_CAMPAIGN: 'group-social',
    NEWSPAPER_AD: 'group-newspaper',
    BILLBOARD_ADVERTISING: 'group-billboard',
    DIGITAL_BANNER: 'group-banner'
  };

  function update(){
    const val = select && select.value || (document.querySelector('input[name="serviceType"]') && document.querySelector('input[name="serviceType"]').value);
    Object.values(groups).forEach(id => {
      const el = document.getElementById(id);
      if(el) el.style.display = 'none';
    });
    if(val && groups[val]){
      const el = document.getElementById(groups[val]);
      if(el) el.style.display = 'block';
    }

    // Clear any accumulated values in form fields when switching services
    if (val) {
      const form = document.querySelector('form');
      if (form) {
        const inputs = form.querySelectorAll('input[type="text"], input[type="number"]');
        inputs.forEach(input => {
          if (input.value && input.value.length > 50) {
            console.warn('Clearing suspiciously long field:', input.name, 'Value:', input.value.substring(0, 50) + '...');
            input.value = '';
          }
        });
      }
    }

    // Sync any .checkbox-group elements into their hidden inputs (comma-separated)
    document.querySelectorAll('.checkbox-group').forEach(cg => {
      const targetField = cg.getAttribute('data-target');
      console.log('Processing checkbox group with target:', targetField);

      // For TV service, use specific ID to avoid conflicts
      let hidden;
      if (targetField === 'optionOne' && cg.closest('#group-tv')) {
        hidden = document.querySelector('#tv-optionOne');
      } else if (targetField === 'optionFour' && cg.closest('#group-tv')) {
        hidden = document.querySelector('#tv-optionFour');
      } else if (targetField === 'optionThree' && cg.closest('#group-social')) {
        hidden = document.querySelector('input[name="optionThree"]');
      } else if (targetField === 'optionOne' && cg.closest('#group-newspaper')) {
        hidden = document.querySelector('input[name="optionOne"]');
      } else if (targetField === 'optionOne' && cg.closest('#group-billboard')) {
        hidden = document.querySelector('input[name="optionOne"]');
      } else {
        hidden = document.querySelector(`input[name="${targetField}"]`);
      }

      console.log('Found hidden field:', hidden);
      if(!hidden) {
        console.log('No hidden field found for target:', targetField);
        return;
      }

      const checkboxes = cg.querySelectorAll('input[type="checkbox"]');

      function updateHiddenValue() {
        const selected = Array.from(checkboxes)
          .filter(cb => cb.checked)
          .map(cb => cb.value);
        hidden.value = selected.join(',');
        console.log('=== CHECKBOX DEBUG ===');
        console.log('Target field:', targetField);
        console.log('All checkboxes:', Array.from(checkboxes).map(cb => ({id: cb.id, checked: cb.checked, value: cb.value})));
        console.log('Selected checkboxes:', selected);
        console.log('Hidden field value:', hidden.value);
        console.log('Number of selected items:', selected.length);
        console.log('=== END CHECKBOX DEBUG ===');

        // Trigger change event to ensure form validation
        hidden.dispatchEvent(new Event('change', { bubbles: true }));
      }

      // Force update on page load to capture any pre-selected values
      updateHiddenValue();

      checkboxes.forEach(cb => {
        cb.addEventListener('change', updateHiddenValue);
        cb.addEventListener('click', updateHiddenValue);
      });

      // Initial update
      updateHiddenValue();
    });
  }

  if(select){
    select.addEventListener('change', update);
  }
  update();

  // Restore checkbox states from hidden field values
  function restoreCheckboxStates() {
    document.querySelectorAll('.checkbox-group').forEach(cg => {
      const targetField = cg.getAttribute('data-target');
      // For TV service, use specific ID to avoid conflicts
      let hidden;
      if (targetField === 'optionOne' && cg.closest('#group-tv')) {
        hidden = document.querySelector('#tv-optionOne');
      } else if (targetField === 'optionFour' && cg.closest('#group-tv')) {
        hidden = document.querySelector('#tv-optionFour');
      } else if (targetField === 'optionThree' && cg.closest('#group-social')) {
        hidden = document.querySelector('input[name="optionThree"]');
      } else {
        hidden = document.querySelector(`input[name="${targetField}"]`);
      }
      if(!hidden || !hidden.value) {
        console.log('No hidden value for field:', targetField);
        return;
      }

      console.log('Restoring checkboxes for field:', targetField, 'Value:', hidden.value);
      const selectedValues = hidden.value.split(',');
      const checkboxes = cg.querySelectorAll('input[type="checkbox"]');

      checkboxes.forEach(cb => {
        if (selectedValues.includes(cb.value)) {
          console.log('Checking checkbox:', cb.value);
          cb.checked = true;
        }
      });
    });
  }

  // Restore checkbox states when page loads
  restoreCheckboxStates();

  // Also restore after a short delay to ensure all elements are loaded
  setTimeout(() => {
    console.log('Delayed restoration attempt');
    restoreCheckboxStates();
  }, 100);

  // Clear any accumulated form data on page load
  function clearFormData() {
    const form = document.querySelector('form');
    if (form) {
      // Clear text and number inputs
      const inputs = form.querySelectorAll('input[type="text"], input[type="number"]');
      inputs.forEach(input => {
        if (input.value && input.value.length > 50) {
          console.warn('Clearing long field on page load:', input.name);
          input.value = '';
        }
      });

  // For edit mode, NUCLEAR RESET of hidden fields
  const isEditMode = document.querySelector('input[name="editId"]') !== null;
  if (isEditMode) {
    console.log('Edit mode detected - NUCLEAR RESET of hidden fields');
    const hiddenInputs = form.querySelectorAll('input[type="hidden"]');
    hiddenInputs.forEach(input => {
      // NUCLEAR RESET: Clear ALL hidden fields in edit mode
      console.log('NUCLEAR RESET - Clearing hidden field:', input.name, 'Value:', input.value);
      input.value = '';
    });

    // Force update all checkbox groups to rebuild from scratch
    document.querySelectorAll('.checkbox-group').forEach(cg => {
      const targetField = cg.getAttribute('data-target');
      const hidden = document.querySelector(`input[name="${targetField}"]`);
      const checkboxes = cg.querySelectorAll('input[type="checkbox"]');

      // Get only the currently checked checkboxes
      const checkedValues = Array.from(checkboxes)
        .filter(cb => cb.checked)
        .map(cb => cb.value);

      // Set the hidden field to only the currently checked values
      hidden.value = checkedValues.join(',');
      console.log('NUCLEAR RESET - Rebuilt field:', targetField, 'Value:', hidden.value);
    });
  }
    }
  }

  // Clear form data when page loads
  clearFormData();

  // Debug function for form data
  window.debugFormData = function() {
    console.log('=== DEBUG FORM DATA ===');
    const form = document.querySelector('form');
    if (form) {
      const formData = new FormData(form);
      for (let [key, value] of formData.entries()) {
        console.log(key + ':', value);
      }

      // Check checkbox groups specifically
      document.querySelectorAll('.checkbox-group').forEach(cg => {
        const targetField = cg.getAttribute('data-target');
        // For TV service, use specific ID to avoid conflicts
        let hidden;
        if (targetField === 'optionOne' && cg.closest('#group-tv')) {
          hidden = document.querySelector('#tv-optionOne');
        } else if (targetField === 'optionFour' && cg.closest('#group-tv')) {
          hidden = document.querySelector('#tv-optionFour');
        } else if (targetField === 'optionThree' && cg.closest('#group-social')) {
          hidden = document.querySelector('input[name="optionThree"]');
        } else if (targetField === 'optionOne' && cg.closest('#group-newspaper')) {
          hidden = document.querySelector('input[name="optionOne"]');
        } else if (targetField === 'optionOne' && cg.closest('#group-billboard')) {
          hidden = document.querySelector('input[name="optionOne"]');
        } else if (targetField === 'optionOne' && cg.closest('#group-banner')) {
          hidden = document.querySelector('input[name="optionOne"]');
        } else {
          hidden = document.querySelector(`input[name="${targetField}"]`);
        }
        const checkboxes = cg.querySelectorAll('input[type="checkbox"]');
        const selected = Array.from(checkboxes).filter(cb => cb.checked).map(cb => cb.value);

        console.log('Checkbox Group:', targetField);
        console.log('  Hidden field value:', hidden ? hidden.value : 'NOT FOUND');
        console.log('  Selected checkboxes:', selected);
        console.log('  Count:', selected.length);
      });
    }
    console.log('=== END DEBUG ===');
  };

  // Add form validation before submission
  const form = document.querySelector('form');
  if (form) {
    form.addEventListener('submit', function(e) {
      console.log('=== FORM SUBMISSION - FORCE UPDATE ALL CHECKBOXES ===');

      // Force update all checkbox groups before submission
      document.querySelectorAll('.checkbox-group').forEach(cg => {
        const targetField = cg.getAttribute('data-target');
        console.log('Force updating checkbox group for target:', targetField);

        // For TV service, use specific ID to avoid conflicts
        let hidden;
        if (targetField === 'optionOne' && cg.closest('#group-tv')) {
          hidden = document.querySelector('#tv-optionOne');
        } else if (targetField === 'optionFour' && cg.closest('#group-tv')) {
          hidden = document.querySelector('#tv-optionFour');
        } else if (targetField === 'optionThree' && cg.closest('#group-social')) {
          hidden = document.querySelector('input[name="optionThree"]');
        } else if (targetField === 'optionOne' && cg.closest('#group-newspaper')) {
          hidden = document.querySelector('input[name="optionOne"]');
        } else if (targetField === 'optionOne' && cg.closest('#group-billboard')) {
          hidden = document.querySelector('input[name="optionOne"]');
        } else if (targetField === 'optionOne' && cg.closest('#group-banner')) {
          hidden = document.querySelector('input[name="optionOne"]');
        } else {
          hidden = document.querySelector(`input[name="${targetField}"]`);
        }

        if(!hidden) {
          console.log('No hidden field found for:', targetField);
          return;
        }

        const checkboxes = cg.querySelectorAll('input[type="checkbox"]');
        const selected = Array.from(checkboxes)
          .filter(cb => cb.checked)
          .map(cb => cb.value);
        hidden.value = selected.join(',');
        console.log('FORCE UPDATE - Field:', targetField, 'Value:', hidden.value);
        console.log('FORCE UPDATE - Selected count:', selected.length);
        console.log('FORCE UPDATE - All checkboxes:', Array.from(checkboxes).map(cb => ({id: cb.id, checked: cb.checked, value: cb.value})));
      });

      // Debug: Log all form data
      console.log('=== FORM SUBMISSION DEBUG ===');
      const formData = new FormData(form);
      for (let [key, value] of formData.entries()) {
        console.log(key + ':', value);
      }
      console.log('=== END FORM DEBUG ===');

      // Check all hidden fields for length
      const hiddenFields = form.querySelectorAll('input[type="hidden"]');
      let hasErrors = false;

      hiddenFields.forEach(field => {
        if (field.value && field.value.length > 255) {
          console.error('Field too long:', field.name, 'Length:', field.value.length);
          hasErrors = true;
        }
      });

      if (hasErrors) {
        e.preventDefault();
        alert('Please refresh the page and try again. Some fields have invalid data.');
        return false;
      }
    });
  }
});






