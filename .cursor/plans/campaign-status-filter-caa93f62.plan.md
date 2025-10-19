<!-- caa93f62-7ed0-466f-8896-4ff003fe35a3 53dc2720-9cde-4534-889e-8231452f91dc -->
# Campaign Page Status Filter Implementation

## How It Will Work (Examples)

### User Experience:

1. **Default view**: Dropdown shows "All Status" - displays all campaigns (REJECTED, APPROVED, PENDING mixed together)
2. **Select "REJECTED"**: Only shows campaigns with REJECTED status
3. **Select "APPROVED"**: Only shows campaigns with APPROVED status  
4. **Select "PENDING"**: Only shows campaigns with PENDING status
5. **Search + Filter**: Can search "john" AND filter by "APPROVED" to find John's approved campaigns

## Files to Modify

### 1. Controller: `src/main/java/com/example/adbridge/controller/AdminCampaignsController.java`

**Change the sorting logic to filtering logic** (lines 76-85):

```java
// REMOVE the current sorting code
// REPLACE with filtering by status:

if (sortBy != null && !sortBy.trim().isEmpty()) {
    // Filter by specific status
    all = all.stream()
            .filter(b -> b.getCampaignStatus() != null && 
                        b.getCampaignStatus().equalsIgnoreCase(sortBy))
            .toList();
}
// If sortBy is null or empty, show all statuses (no filtering)
```

### 2. Template: `src/main/resources/templates/admin/campaigns/list.html`

**Update the dropdown options** (lines 26-29):

```html
<select name="sortBy" class="sort-dropdown" onchange="this.form.submit()">
    <option value="">All Status</option>
    <option value="REJECTED" th:selected="${sortBy == 'REJECTED'}">REJECTED</option>
    <option value="APPROVED" th:selected="${sortBy == 'APPROVED'}">APPROVED</option>
    <option value="PENDING" th:selected="${sortBy == 'PENDING'}">PENDING</option>
</select>
```

## Result

The campaigns page will now work exactly like:

- **Documents page**: Filter by staff member (All Staff, Admin, Finance Coordinator...)
- **Support page**: Filter by priority (All Priorities, High, Medium, Low)
- **Campaigns page**: Filter by status (All Status, REJECTED, APPROVED, PENDING)

Search still works across all fields (booking ID, client name, company, service type, status).