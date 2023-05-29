package io.github.lgp547.anydoor.test.dto;


import java.util.Objects;

public class Permission {
    private Long id;
    private String text;

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

//    public Permission() {
//    }

    public Permission(Long id, String text) {
        this.id = id;
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(id, that.id) && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text);
    }

    public static class PermissionBuilder {
        private Long id;
        private String text;

        PermissionBuilder() {
        }

        public Permission.PermissionBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public Permission.PermissionBuilder text(String text) {
            this.text = text;
            return this;
        }

        public Permission build() {
            return new Permission(this.id, this.text);
        }

        public String toString() {
            return "Permission.PermissionBuilder(id=" + this.id + ", text=" + this.text + ")";
        }
    }

}
