package ru.sibsutis.project.crud;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import ru.sibsutis.project.AddProductFaultException;
import ru.sibsutis.project.NotFoundException;
import ru.sibsutis.project.databases.Product;
import ru.sibsutis.project.databases.User;
import ru.sibsutis.project.dto.ProductDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public List<Product> getAll() {
        return repository.findAllByStatus(true);
    }

    public Product create(ProductDto productDto, Long userID) {
        Product product = new Product();
        BeanUtils.copyProperties(productDto, product, "name");
        product.setName(productDto.getName().toLowerCase());
        User owner = userRepository.findById(userID).orElseThrow(NotFoundException::new);
        product.setOwner(owner);
        product.setStatus(true);//В КОНЕЧНОЙ ВЕРСИИ БУДЕТ ИЗНАЧАЛЬНО false. На true меняется при поступлении товара в офис
        return repository.save(product);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Product> getById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        List<Product> products = repository.findByOwner(user);
        return products.stream()
                .filter(Product::getStatus)
                .collect(Collectors.toList());
    }

    public List<Product> getFavesById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        return user.getFavorites();
    }

    public List<Product> getExchangesByProductId(Long productId) {
        return repository.findById(productId).orElseThrow(NotFoundException::new).getProductsForExchange();
    }

    public List<Product> getProductsByCategory(String category) {
        return repository.findByCategory(category);
    }

    public void addFromProfile(Long productId, List<Long> productsId) {
        Product product = repository.findById(productId).orElseThrow(NotFoundException::new);
        List<Product> products = getProductsById(productId, productsId);
        products.forEach(product::addToExchange);
        repository.save(product);
    }


    public void addFromHome(Long productId, List<Long> productsId) {
        Product product = repository.findById(productId).orElseThrow(NotFoundException::new);
        List<Product> products = getProductsById(productId, productsId);
        products.forEach(p -> p.addToExchange(product));
        repository.save(product);
    }

    private List<Product> getProductsById(Long productId, List<Long> productsId) {
        if (productsId.contains(productId)) {
            throw new AddProductFaultException("same_product");
        }
        return productsId.stream()
                .map(repository::findById)
                .map(op -> op.orElse(null))
                .collect(Collectors.toList());
    }

    public void deleteExchange(Long availableId, Long exchangeId) {
        Product available = repository.findById(availableId).orElseThrow(NotFoundException::new);
        Product exchange = repository.findById(exchangeId).orElseThrow(NotFoundException::new);
        available.deleteFromExchange(exchange);
        repository.save(available);
    }

    public Product productInfo(Long productId) {
        return repository.findById(productId).orElseThrow(NotFoundException::new);
    }

    public boolean isFaves(Long productId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        Product product = repository.findById(productId).orElseThrow(NotFoundException::new);
        return user.getFavorites().contains(product);
    }

    public List<Product> getByName(String name) {
        List<Product> products = repository.findAll();
        return products.stream()
                .filter(product -> product.getName().contains(name))
                .collect(Collectors.toList());
    }

    public void deleteProductAfterExchange(Long id) {
        Product p = repository.findById(id).orElseThrow(NotFoundException::new);
        p.setStatus(false);
        p.setProductsForExchange(null);
        p.setUsersFaves(null);
        repository.save(p);
    }


}
