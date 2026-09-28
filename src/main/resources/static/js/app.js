// Utility to display messages
function showMessage(type, message) {
    const alertBox = document.getElementById('alert-box');
    if (!alertBox) return;
    
    alertBox.className = `alert alert-${type}`;
    alertBox.textContent = message;
    alertBox.style.display = 'block';
    
    setTimeout(() => {
        alertBox.style.display = 'none';
    }, 5000);
}

// Utility to fetch API
async function apiCall(url, options = {}) {
    try {
        const response = await fetch(url, options);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'An error occurred');
        }
        
        return data;
    } catch (error) {
        throw error;
    }
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR'
    }).format(amount);
}

// Format Date
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-IN');
}

// Load groups for dropdowns
async function loadGroupsDropdown(selectId) {
    const select = document.getElementById(selectId);
    if (!select) return;
    
    try {
        const groups = await apiCall('/api/groups');
        select.innerHTML = '<option value="">Select a Group</option>';
        groups.forEach(group => {
            select.innerHTML += `<option value="${group.groupId}">${group.groupName}</option>`;
        });
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

// Load members for dropdowns
async function loadMembersDropdown(selectId) {
    const select = document.getElementById(selectId);
    if (!select) return;
    
    try {
        const members = await apiCall('/api/members');
        if (!members || members.length === 0) {
            select.innerHTML = '<option value="">No members found (Add one in Members tab)</option>';
            return;
        }
        select.innerHTML = '<option value="">-- Select a Member --</option>';
        members.forEach(member => {
            const groupName = member.group ? member.group.groupName : 'No Group';
            select.innerHTML += `<option value="${member.memberId}">${member.name} (${groupName})</option>`;
        });
    } catch (error) {
        console.error('Error loading members:', error);
    }
}

// Load active loans for dropdowns
async function loadActiveLoansDropdown(selectId) {
    const select = document.getElementById(selectId);
    if (!select) return;
    
    try {
        const loans = await apiCall('/api/loans');
        const activeLoans = loans.filter(l => l.status === 'ACTIVE');
        
        select.innerHTML = '<option value="">Select an Active Loan</option>';
        activeLoans.forEach(loan => {
            select.innerHTML += `<option value="${loan.loanId}">Loan #${loan.loanId} - ${loan.member.name} (Amount: ${formatCurrency(loan.amount)})</option>`;
        });
    } catch (error) {
        console.error('Error loading loans:', error);
    }
}

// Handle form submissions generic function
function setupFormSubmit(formId, apiUrl, successMessage, callback) {
    const form = document.getElementById(formId);
    if (!form) return;
    
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const formData = new FormData(form);
        const data = Object.fromEntries(formData.entries());
        
        for (const key in data) {
            if (data[key] === "") {
                data[key] = null;
            }
        }
        
        try {
            await apiCall(apiUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });
            
            showMessage('success', successMessage);
            form.reset();
            
            if (callback) callback();
            
        } catch (error) {
            showMessage('danger', error.message);
        }
    });
}
