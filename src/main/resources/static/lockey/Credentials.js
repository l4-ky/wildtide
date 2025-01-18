class Credentials {
    constructor(name, fields1, fields2, isPinned) {
        this.name = name;
        this.topFields = fields1;
        this.bottomFields = fields2;
        this.isPinned = isPinned;
    }
    // Method to add a top field
    addTopField(field) {
        this.topFields.push(field);
    }
    // Method to add a bottom field
    addBottomField(field) {
        this.bottomFields.push(field);
    }
    // Method to remove a top field
    removeTopField(field) {
        const index = this.topFields.indexOf(field);
        if (index > -1) {
            this.topFields.splice(index, 1);
        }
    }
    // Method to remove a bottom field
    removeBottomField(field) {
        const index = this.bottomFields.indexOf(field);
        if (index > -1) {
            this.bottomFields.splice(index, 1);
        }
    }
}